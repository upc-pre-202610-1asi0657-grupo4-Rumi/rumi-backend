# Despliegue local con Docker Compose

Levanta los nueve servicios de Rumi con PostgreSQL, TimescaleDB y RabbitMQ.

```bash
mkdir rumi && cd rumi
git clone https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-backend.git
bash rumi-backend/deploy/clone-all.sh
cd rumi-backend/deploy
docker compose up --build
docker ps
curl http://localhost:8080/api/v1/buildings
```

| Servicio | Puerto |
|---|---|
| api-gateway | 8080 |
| building / monitoring / seismic | 8081 / 8082 / 8083 |
| iam / notification / safety / billing / maintenance | 8084 a 8088 |
| PostgreSQL / TimescaleDB | 5432 / 5433 |
| RabbitMQ AMQP / management | 5672 / 15672 |

Las credenciales locales para las bases de datos y RabbitMQ son `rumi` / `rumi`.
RabbitMQ Management queda disponible en `http://localhost:15672`.

`notificationsdb` tambien se aloja en PostgreSQL. Se crea junto con sus tablas desde
`rumi-notification-service/docs/schema.sql` al inicializar el volumen. Monitoring,
Seismic, Notifications, Building e IAM se conectan a RabbitMQ.

Building publica `BuildingRegistered` en el exchange `rumi.building-management.events` mediante un
outbox transaccional (tabla `outbox_events`, decisión D3.9) e IAM lo consume en la cola
`rumi.iam.building-registered` para vincular al administrador con su edificio. Si RabbitMQ no
está disponible, el evento espera en el outbox y se publica cuando vuelve. Las tablas
`outbox_events` (buildingsdb) y `processed_events` (iamdb) vienen de los `docs/schema.sql` de cada
servicio: si tu volumen se creó antes de este cambio, ejecuta una vez `docker compose down -v`.

`iamdb` tambien se aloja en PostgreSQL y se crea desde `rumi-iam-service/docs/schema.sql`.
Los scripts de inicializacion solo se ejecutan con un volumen vacio; para recrear las
bases de datos usa `docker compose down -v`. Antes de `docker compose up` exporta
`JWT_SECRET` (al menos 32 caracteres), por ejemplo `export JWT_SECRET=...`.

`billingdb` tambien se aloja en PostgreSQL y se crea con sus tablas y los dos planes desde
`rumi-billing-service/docs/schema.sql` (script `05-create-billing-database.sql`). Si ya
tenias el volumen creado antes de este cambio, ejecuta una vez `docker compose down -v`
para que se cree `billingdb`. Billing no necesita `JWT_SECRET`: el gateway valida el token.

`buildingsdb` y `seismiceventsdb` se alojan en PostgreSQL; `sensorreadingsdb` se
aloja en TimescaleDB, con la extensión `timescaledb` habilitada. Los datos persisten
en volúmenes de Docker.

Los Dockerfile están en cada repositorio de servicio.

## Prueba de punta a punta

`scripts/e2e-building-access.sh` recorre, contra el gateway (`http://localhost:8080`), el flujo de
acceso por edificio:

1. Espera a que los servicios hayan arrancado.
2. Registra e inicia sesión con dos administradores (A y B).
3. A registra un edificio; comprueba que el evento pasó por `outbox_events` (con `published_at`) y
   que IAM creó el vínculo en `user_building_ids` y `processed_events`.
4. A reemite su token (`POST /api/v1/users/me/token`) hasta que `buildingIds` incluye el edificio.
5. `GET /api/v1/alerts?buildingId=<edificio>`: A → 200, B → 403, sin token → 401.
6. Reinicia building-service; detiene RabbitMQ, registra otro edificio y comprueba que queda pendiente
   en el outbox; levanta RabbitMQ y comprueba que se publica y se vincula.

```bash
cd rumi-backend
bash deploy/scripts/e2e-building-access.sh            # con el stack ya levantado
RESET=1 bash deploy/scripts/e2e-building-access.sh    # antes hace docker compose down -v && up --build -d
```

- Necesita `docker compose`, `curl` y `python` (solo para leer JSON) y `deploy/.env` con `JWT_SECRET`.
- Cada ejecución usa correos nuevos, así que se puede repetir sin recrear los volúmenes.
- `RESET=1` borra los volúmenes locales.
- Termina con código 0 si todos los chequeos pasan; si no, el código es el número de fallos.
- `SERVICES` cambia la lista de servicios que espera. Hoy safety y maintenance no arrancan en Compose
  (les faltan las variables de base de datos), así que hay que excluirlos:

```bash
SERVICES="rumi-api-gateway rumi-building-service rumi-monitoring-service rumi-seismic-service rumi-iam-service rumi-notification-service rumi-billing-service" \
  bash deploy/scripts/e2e-building-access.sh
```
