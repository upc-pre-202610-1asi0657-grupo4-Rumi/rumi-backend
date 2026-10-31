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
`rumi-notification-service/docs/schema.sql` al inicializar el volumen. Monitoring y
Notifications se conectan a RabbitMQ.

`iamdb` tambien se aloja en PostgreSQL y se crea desde `rumi-iam-service/docs/schema.sql`.
Los scripts de inicializacion solo se ejecutan con un volumen vacio; para recrear las
bases de datos usa `docker compose down -v`. Antes de `docker compose up` exporta
`JWT_SECRET` (al menos 32 caracteres), por ejemplo `export JWT_SECRET=...`.

`buildingsdb` y `seismiceventsdb` se alojan en PostgreSQL; `sensorreadingsdb` se
aloja en TimescaleDB, con la extensión `timescaledb` habilitada. Los datos persisten
en volúmenes de Docker.

Los Dockerfile están en cada repositorio de servicio.
