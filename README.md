# rumi-backend

Repositorio de origen del backend de **Rumi** (Kuntur Labs): el monolito modular a partir del cual se descompuso el sistema en microservicios, más el despliegue conjunto de esos microservicios.

> **Estado:** el monolito se conserva como línea base (tag `monolith-baseline`). El desarrollo activo ocurre en los repositorios de cada servicio.

## Contenido

| Ruta | Descripción |
|---|---|
| `src/` | Monolito modular (Spring Boot, Java 21) con los contextos Building Management, Structural Monitoring y Seismic Correlation |
| `docs/refactoring/` | Informes de la descomposición: acoplamiento, base de datos por contexto, contratos de eventos y decisiones |
| `docs/endpoint-inventory.md` | Inventario de endpoints del monolito |
| `docs/postman/` | Colección de Postman del Sprint 1, que consulta los servicios a través del API Gateway |
| `deploy/` | `docker-compose.yml` para levantar los nueve microservicios juntos, y `clone-all.sh` |

## Microservicios

| Servicio | Puerto | Estado en el Sprint 1 |
|---|---|---|
| [rumi-api-gateway](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-api-gateway) | 8080 | Enrutamiento |
| [rumi-building-service](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-building-service) | 8081 | API REST implementada |
| [rumi-monitoring-service](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-monitoring-service) | 8082 | API REST implementada |
| [rumi-seismic-service](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-seismic-service) | 8083 | API REST implementada |
| [rumi-iam-service](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-iam-service) | 8084 | Estructura inicial |
| [rumi-notification-service](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-notification-service) | 8085 | Estructura inicial |
| [rumi-safety-service](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-safety-service) | 8086 | Estructura inicial |
| [rumi-billing-service](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-billing-service) | 8087 | Estructura inicial |
| [rumi-maintenance-service](https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-maintenance-service) | 8088 | Estructura inicial |

Cada servicio es independiente: tiene su propio repositorio, su Dockerfile y su flujo de integración continua. El archivo `deploy/docker-compose.yml` no pertenece a ninguno de ellos; solo indica qué contenedores se levantan juntos para pruebas locales.

## Ejecutar todos los servicios con Docker Compose

Requisitos: Docker con Compose v2 y Git. No se necesita PostgreSQL ni RabbitMQ, porque los servicios arrancan con el perfil `dev` (H2 en memoria).

```bash
mkdir rumi && cd rumi
git clone https://github.com/upc-pre-202610-1asi0657-grupo4-Rumi/rumi-backend.git
bash rumi-backend/deploy/clone-all.sh      # clona los nueve servicios al lado
cd rumi-backend/deploy
docker compose up --build
```

`docker compose` debe ejecutarse desde `deploy/`, porque los contextos de construcción son relativos (`../../rumi-*`).

### Verificación

```bash
docker ps    # nueve contenedores en estado Up
curl http://localhost:8080/api/v1/buildings/7a9b3c1d-2e4f-4b6a-8c0d-1e2f3a4b5c6d
curl http://localhost:8080/api/v1/authentication/health
```

La primera consulta llega a `rumi-building-service` a través del gateway y devuelve el edificio de ejemplo (Torre Miraflores). La segunda llega a `rumi-iam-service` y responde `{"status":"UP"}`.

## Ejecutar el monolito (línea base)

Requiere JDK 21 y Maven instalados (este repositorio no incluye Maven wrapper) y la configuración de base de datos y mensajería de `application.yml`.

```bash
git checkout monolith-baseline
mvn spring-boot:run
```

## Flujo de trabajo

GitFlow: `main` para versiones estables, `develop` para integración y ramas `feature/*` con pull request hacia `develop`. Los commits siguen [Conventional Commits](https://www.conventionalcommits.org/) (`tipo(alcance): descripción`).

## Documentación

- Descomposición en microservicios: [`docs/refactoring/decomposition-report.md`](docs/refactoring/decomposition-report.md)
- Acoplamiento entre contextos: [`docs/refactoring/coupling-report.md`](docs/refactoring/coupling-report.md)
- Base de datos por contexto: [`docs/refactoring/database-per-context.md`](docs/refactoring/database-per-context.md)
- Contratos de eventos: [`docs/refactoring/event-contracts.md`](docs/refactoring/event-contracts.md)
