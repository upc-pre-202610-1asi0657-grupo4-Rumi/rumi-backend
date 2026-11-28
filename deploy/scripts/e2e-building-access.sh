#!/usr/bin/env bash
# End-to-end check through the API gateway of:
#   - building registration -> transactional outbox -> RabbitMQ -> IAM building membership -> token reissue;
#   - building access in Notification (own building 200, other administrator 403, no token 401);
#   - the outbox surviving a building-service restart and a RabbitMQ outage.
#
# Usage (from anywhere; it works in rumi-backend/deploy, where deploy/.env must hold JWT_SECRET):
#   bash deploy/scripts/e2e-building-access.sh            # against the stack that is already running
#   RESET=1 bash deploy/scripts/e2e-building-access.sh    # first docker compose down -v && up --build -d
#                                                          # (deletes the local volumes!)
# Optional: BASE (default http://localhost:8080), SERVICES (services to wait for).
# Needs: docker compose, curl, python (only to read JSON). Exit code = number of failed checks.
set -uo pipefail

cd "$(dirname "$0")/.." || exit 1
BODY="$(mktemp)"
trap 'rm -f "$BODY"' EXIT

BASE="${BASE:-http://localhost:8080}"
PASSWORD="Sismo-Seguro-2026"
RUN="$(date +%s)"
ADMIN_A="admin.a.${RUN}@rumi.dev"
ADMIN_B="admin.b.${RUN}@rumi.dev"
SERVICES="${SERVICES:-rumi-api-gateway rumi-building-service rumi-monitoring-service rumi-seismic-service rumi-iam-service
          rumi-notification-service rumi-safety-service rumi-billing-service rumi-maintenance-service}"
FAILURES=0

step()  { printf '\n=== %s\n' "$*"; }
pass()  { printf '  PASS  %s\n' "$*"; }
fail()  { printf '  FAIL  %s\n' "$*"; FAILURES=$((FAILURES + 1)); }
check() { if [ "$2" = "$3" ]; then pass "$1 ($3)"; else fail "$1: expected $2, got $3"; fi; }
json()  { python -c "import sys,json; d=json.load(sys.stdin); print(eval('d' + sys.argv[1]))" "$1"; }
sql()   { docker compose exec -T postgres psql -U rumi -d "$1" -tAc "$2" | tr -d '\r'; }

# Waits until every Spring Boot container has logged its "Started ...Application" line.
wait_started() {
  for service in "$@"; do
    printf '  waiting for %s' "$service"
    for _ in $(seq 1 120); do
      if docker compose logs --since 30m "$service" 2>/dev/null | grep -q "Started .*Application"; then
        printf ' ... up\n'; continue 2
      fi
      printf '.'; sleep 2
    done
    printf ' ... TIMEOUT\n'; fail "$service did not start"
  done
}

wait_healthy() {  # $1 = compose service with a healthcheck
  for _ in $(seq 1 60); do
    [ "$(docker inspect -f '{{.State.Health.Status}}' "$(docker compose ps -q "$1")" 2>/dev/null)" = healthy ] && return 0
    sleep 2
  done
  fail "$1 is not healthy"
}

# POST JSON; prints "<status> <body>"
post() {  # $1 path, $2 body, $3 token (optional)
  local auth=()
  [ -n "${3:-}" ] && auth=(-H "Authorization: Bearer $3")
  curl -s -o "$BODY" -w '%{http_code}' -X POST "$BASE$1" -H "Content-Type: application/json" "${auth[@]}" -d "$2"
  printf ' '; cat "$BODY"
}

get_status() {  # $1 path, $2 token (optional)
  local auth=()
  [ -n "${2:-}" ] && auth=(-H "Authorization: Bearer $2")
  curl -s -o /dev/null -w '%{http_code}' "$BASE$1" "${auth[@]}"
}

# Reissues the token of A until buildingIds contains $1 (IAM links the building asynchronously).
reissue_until_linked() {  # $1 buildingId
  local response status
  for attempt in $(seq 1 20); do
    response="$(post /api/v1/users/me/token '{}' "$TOKEN_A")"
    status="${response%% *}"
    if [ "$status" = 200 ] && printf '%s' "${response#* }" | json "['buildingIds']" | grep -q "$1"; then
      TOKEN_A="$(printf '%s' "${response#* }" | json "['token']")"
      pass "reissued token of A contains building $1 (attempt $attempt)"
      return 0
    fi
    sleep 0.5
  done
  fail "buildingIds of A never contained $1 (last answer: $response)"
}

register_building() {  # $1 name -> prints the building id
  post /api/v1/buildings \
    "{\"name\":\"$1\",\"address\":\"Av. Larco 1234, Miraflores\",\"floors\":12,\"constructionYear\":2015,\"units\":48}" \
    "$TOKEN_A"
}

# ---------------------------------------------------------------------------------------------------------
if [ "${RESET:-0}" = 1 ]; then
  step "1. docker compose down -v && up --build -d"
  docker compose down -v
  docker compose up --build -d || { fail "docker compose up"; exit 1; }
fi
step "1. Wait until every service is up"
wait_healthy postgres; wait_healthy rabbitmq
# shellcheck disable=SC2086
wait_started $SERVICES
check "gateway health" 200 "$(get_status /api/v1/gateway/health)"

# ---------------------------------------------------------------------------------------------------------
step "2. Sign-up and sign-in of administrators A and B"
for who in A B; do
  email_var="ADMIN_$who"; email="${!email_var}"
  response="$(post /api/v1/authentication/sign-up "{\"email\":\"$email\",\"password\":\"$PASSWORD\",\"fullName\":\"Administrador $who\"}")"
  check "sign-up $who" 201 "${response%% *}"
  response="$(post /api/v1/authentication/sign-in "{\"email\":\"$email\",\"password\":\"$PASSWORD\"}")"
  check "sign-in $who" 200 "${response%% *}"
  printf -v "TOKEN_$who" '%s' "$(printf '%s' "${response#* }" | json "['token']")"
  printf -v "USER_$who" '%s' "$(printf '%s' "${response#* }" | json "['userId']")"
done
echo "  A=$USER_A  B=$USER_B"

# ---------------------------------------------------------------------------------------------------------
step "3. A registers a building; the event goes through the outbox and IAM links it"
response="$(register_building "Torre E2E A $RUN")"
check "POST /api/v1/buildings by A" 201 "${response%% *}"
BUILDING_1="$(printf '%s' "${response#* }" | json "['id']")"
check "administratorUserId is A (from the token, not the body)" "$USER_A" \
      "$(printf '%s' "${response#* }" | json "['administratorUserId']")"
for _ in $(seq 1 30); do
  published="$(sql buildingsdb "SELECT published_at IS NOT NULL FROM outbox_events WHERE aggregate_id = '$BUILDING_1'")"
  [ "$published" = t ] && break; sleep 1
done
check "outbox_events row of the building has published_at" t "$published"
sql buildingsdb "SELECT event_type, attempts, published_at FROM outbox_events WHERE aggregate_id = '$BUILDING_1'" | sed 's/^/  outbox: /'
for _ in $(seq 1 30); do
  linked="$(sql iamdb "SELECT count(*) FROM user_building_ids WHERE user_id = '$USER_A' AND building_id = '$BUILDING_1'")"
  [ "$linked" = 1 ] && break; sleep 1
done
check "IAM user_building_ids links A to the building" 1 "$linked"
EVENT_1="$(sql buildingsdb "SELECT event_id FROM outbox_events WHERE aggregate_id = '$BUILDING_1'")"
check "IAM processed_events has the event once" 1 \
      "$(sql iamdb "SELECT count(*) FROM processed_events WHERE event_id = '$EVENT_1'")"

# ---------------------------------------------------------------------------------------------------------
step "4. A reissues its token until buildingIds contains the building"
reissue_until_linked "$BUILDING_1"

# ---------------------------------------------------------------------------------------------------------
step "5. Building access in Notification"
check "A: GET /api/v1/alerts?buildingId=<own building>" 200 "$(get_status "/api/v1/alerts?buildingId=$BUILDING_1" "$TOKEN_A")"
check "B: GET /api/v1/alerts?buildingId=<building of A>" 403 "$(get_status "/api/v1/alerts?buildingId=$BUILDING_1" "$TOKEN_B")"
check "no token: GET /api/v1/alerts?buildingId=..." 401 "$(get_status "/api/v1/alerts?buildingId=$BUILDING_1")"

# ---------------------------------------------------------------------------------------------------------
step "6a. Restart only building-service (broker up)"
docker compose stop rumi-building-service >/dev/null
docker compose start rumi-building-service >/dev/null
SINCE="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
for _ in $(seq 1 90); do
  docker compose logs --since "$SINCE" rumi-building-service 2>/dev/null | grep -q "Started .*Application" && break
  sleep 2
done
check "building answers through the gateway after restart" 200 "$(get_status "/api/v1/buildings/$BUILDING_1" "$TOKEN_A")"
check "outbox has no pending events after restart" 0 \
      "$(sql buildingsdb "SELECT count(*) FROM outbox_events WHERE published_at IS NULL")"

step "6b. RabbitMQ down: a new building waits in the outbox"
docker compose stop rabbitmq >/dev/null
response="$(register_building "Torre E2E Broker Caido $RUN")"
check "POST /api/v1/buildings by A with RabbitMQ down" 201 "${response%% *}"
BUILDING_2="$(printf '%s' "${response#* }" | json "['id']")"
sleep 8
row="$(sql buildingsdb "SELECT published_at IS NULL, attempts > 0, last_error IS NOT NULL FROM outbox_events WHERE aggregate_id = '$BUILDING_2'")"
check "event pending (published_at NULL, attempts > 0, last_error set)" "t|t|t" "$row"
sql buildingsdb "SELECT attempts, next_attempt_at, left(last_error, 90) FROM outbox_events WHERE aggregate_id = '$BUILDING_2'" | sed 's/^/  outbox: /'
check "IAM not linked yet" 0 "$(sql iamdb "SELECT count(*) FROM user_building_ids WHERE building_id = '$BUILDING_2'")"

step "6c. RabbitMQ back: the event is published and linked"
docker compose start rabbitmq >/dev/null
wait_healthy rabbitmq
for _ in $(seq 1 120); do
  published="$(sql buildingsdb "SELECT published_at IS NOT NULL FROM outbox_events WHERE aggregate_id = '$BUILDING_2'")"
  [ "$published" = t ] && break; sleep 2
done
check "event published after RabbitMQ is back" t "$published"
sql buildingsdb "SELECT attempts, published_at FROM outbox_events WHERE aggregate_id = '$BUILDING_2'" | sed 's/^/  outbox: /'
for _ in $(seq 1 60); do
  linked="$(sql iamdb "SELECT count(*) FROM user_building_ids WHERE user_id = '$USER_A' AND building_id = '$BUILDING_2'")"
  [ "$linked" = 1 ] && break; sleep 1
done
check "IAM links A to the second building" 1 "$linked"
reissue_until_linked "$BUILDING_2"
check "A: GET /api/v1/alerts?buildingId=<second building>" 200 "$(get_status "/api/v1/alerts?buildingId=$BUILDING_2" "$TOKEN_A")"

printf '\n%s\n' "$([ "$FAILURES" = 0 ] && echo 'ALL CHECKS PASSED' || echo "$FAILURES CHECK(S) FAILED")"
exit "$FAILURES"
