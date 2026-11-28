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
