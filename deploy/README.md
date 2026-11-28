# Despliegue local con Docker Compose

Levanta los nueve servicios de Rumi con PostgreSQL, TimescaleDB y RabbitMQ.

```bash
mkdir rumi && cd rumi
git clone https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-backend.git
bash rumi-backend/deploy/clone-all.sh
cd rumi-backend/deploy
cp .env.example .env      # y reemplaza change-me: openssl rand -base64 48
docker compose up --build
docker ps
curl http://localhost:8080/api/v1/gateway/health
```

## Puertos

Solo el gateway se publica en la red: todas las peticiones pasan por él y por su validación del JWT.
La infraestructura se publica únicamente en `127.0.0.1` (no es accesible desde otras máquinas) y los
microservicios no publican puertos: el gateway los alcanza por la red interna `rumi-net`.

| Componente | Puerto en el host | Interno |
|---|---|---|
| api-gateway | `8080` (todas las interfaces) | 8080 |
| building / monitoring / seismic | no se publica | 8081 / 8082 / 8083 |
| iam / notification / safety / billing / maintenance | no se publica | 8084 a 8088 |
| PostgreSQL / TimescaleDB | `127.0.0.1:5432` / `127.0.0.1:5433` | 5432 |
| RabbitMQ AMQP / management | `127.0.0.1:5672` / `127.0.0.1:15672` | 5672 / 15672 |

Las credenciales locales para las bases de datos y RabbitMQ son `rumi` / `rumi`.
RabbitMQ Management queda disponible en `http://localhost:15672`. Los tests de
`rumi-notification-service` siguen usando el RabbitMQ local en `127.0.0.1:5672`.

### Puertos de los servicios para depurar

Para abrir el Swagger UI de un servicio o llamarlo sin el gateway, añade `docker-compose.debug.yml`,
que publica los puertos 8081 a 8088 **solo en `127.0.0.1`**:

```bash
docker compose -f docker-compose.yml -f docker-compose.debug.yml up -d
# por ejemplo: http://localhost:8085/swagger-ui.html
```

Sin el `-f`, los servicios no publican puertos. **No renombres este archivo a
`docker-compose.override.yml`**: Compose lo cargaría automáticamente en cada `docker compose up` y
volvería a publicar los puertos de todos los servicios.

`JWT_SECRET` es obligatorio: si falta en `.env`, `docker compose` se detiene con el mensaje
`Set JWT_SECRET (at least 32 characters) before running docker compose`. El valor `change-me` de
`.env.example` no sirve: el gateway e IAM exigen al menos 32 caracteres.

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

`evacuationdb` (Safety) y `maintenancedb` (Maintenance) tambien se alojan en PostgreSQL y se crean con
sus tablas desde `rumi-safety-service/docs/schema.sql` y `rumi-maintenance-service/docs/schema.sql`
(scripts `06-create-evacuation-database.sql` y `07-create-maintenance-database.sql`). Ninguno de los
dos usa RabbitMQ.

`buildingsdb` y `seismiceventsdb` se alojan en PostgreSQL; `sensorreadingsdb` se
aloja en TimescaleDB, con la extensión `timescaledb` habilitada. Los datos persisten
en volúmenes de Docker.

### Scripts de inicialización y volúmenes

Los scripts de `postgres/init` y los `docs/schema.sql` montados solo se ejecutan cuando el volumen de
PostgreSQL está vacío, es decir, la primera vez. Si tu volumen se creó antes de que se añadiera una base
o una tabla, esa base no existe y el servicio que la usa no arranca. Para recrearlas:

```bash
docker compose down -v   # borra TODOS los datos locales (bases y RabbitMQ)
docker compose up --build -d
```

Usa `down -v` solo si aceptas perder esos datos; `docker compose down` (sin `-v`) o
`docker compose stop` los conservan. El healthcheck de PostgreSQL comprueba por TCP
(`pg_isready -h 127.0.0.1`), así que los servicios esperan a que terminen los scripts de init.
Los montajes nuevos usan la sintaxis larga con `create_host_path: false`: si falta un `docs/schema.sql`,
Compose falla en vez de montar un directorio vacío.

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
- `SERVICES` cambia la lista de servicios que espera (por defecto, los nueve).
- Para otro proyecto de Compose, exporta `COMPOSE_PROJECT_NAME` antes de ejecutarlo.
