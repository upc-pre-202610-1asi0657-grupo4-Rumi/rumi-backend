# Despliegue local con Docker Compose

Levanta los nueve servicios de Rumi (perfil `dev`, sin PostgreSQL ni RabbitMQ).

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

Los Dockerfile están en cada repositorio de servicio.
