# Endpoint Inventory

One row per REST endpoint, with the service that owns it after the decomposition.

- `implemented`: functional endpoint wired end to end (controller, application service, persistence).
- `partial`: exposed but incomplete.
- `skeleton`: health endpoint of a service skeleton. It is NOT a functional endpoint and must not be
  counted as implemented.

First version of this inventory: monolith at `bf8bc87` (`monolith-baseline`).
This version: services extracted from `rumi-backend@3ab07ec`, each tagged `v0.1.0`.

## Functional endpoints

| # | Service | Port | Verb | Path | Controller#method | Request DTO | Response DTO | User story | Status |
|---|---|---|---|---|---|---|---|---|---|
| 1 | `rumi-building-service` | 8081 | POST | `/api/v1/buildings` | `BuildingController#registerBuilding` | `BuildingRequestDto` (`name`, `address`) | `BuildingResponseDto` (`id`, `name`, `address`), HTTP 201 | US04 register building | implemented |

Notes on endpoint 1:

- In the monolith baseline the path was `/api/buildings`; it was versioned to `/api/v1/buildings` during the refactoring.
- There is no bean validation and no exception handler: a blank `name` or `address` raises
  `IllegalArgumentException` in the `Building` constructor and is returned as HTTP 500.

## Skeleton endpoints (not functional)

| # | Service | Port | Verb | Path | Controller#method | Request DTO | Response DTO | User story | Status |
|---|---|---|---|---|---|---|---|---|---|
| S1 | `rumi-iam-service` | 8084 | GET | `/api/v1/authentication/health` | `HealthController#health` | none | `HealthResponse` (`status`, `service`) | none | skeleton |
| S2 | `rumi-notification-service` | 8085 | GET | `/api/v1/alerts/health` | `HealthController#health` | none | `HealthResponse` | none | skeleton |
| S3 | `rumi-safety-service` | 8086 | GET | `/api/v1/evacuation-plans/health` | `HealthController#health` | none | `HealthResponse` | none | skeleton |
| S4 | `rumi-billing-service` | 8087 | GET | `/api/v1/subscriptions/health` | `HealthController#health` | none | `HealthResponse` | none | skeleton |
| S5 | `rumi-maintenance-service` | 8088 | GET | `/api/v1/inspections/health` | `HealthController#health` | none | `HealthResponse` | none | skeleton |
| S6 | `rumi-api-gateway` | 8080 | GET | `/api/v1/gateway/health` | `HealthController#health` | none | `HealthResponse` | none | skeleton |

## Services without REST endpoints

| Service | Port | What exists instead |
|---|---|---|
| `rumi-monitoring-service` | 8082 | `SensorReadingRecordedPublisher` (RabbitMQ) |
| `rumi-seismic-service` | 8083 | `SensorReadingRecordedSubscriber` (RabbitMQ); `IgpFeedClient` (US13) and `RiskIndexCalculationService` (US11) exist but are not exposed over REST |

## Application-layer operations not exposed over REST

| Service | Operation | Possible endpoint | User story |
|---|---|---|---|
| `rumi-building-service` | `BuildingApplicationService#findBuildingById` | `GET /api/v1/buildings/{buildingId}` | US04 |

## Summary

| Service | Port | Implemented | Partial | Skeleton |
|---|---|---|---|---|
| `rumi-building-service` | 8081 | 1 | 0 | 0 |
| `rumi-monitoring-service` | 8082 | 0 | 0 | 0 |
| `rumi-seismic-service` | 8083 | 0 | 0 | 0 |
| `rumi-iam-service` | 8084 | 0 | 0 | 1 |
| `rumi-notification-service` | 8085 | 0 | 0 | 1 |
| `rumi-safety-service` | 8086 | 0 | 0 | 1 |
| `rumi-billing-service` | 8087 | 0 | 0 | 1 |
| `rumi-maintenance-service` | 8088 | 0 | 0 | 1 |
| `rumi-api-gateway` | 8080 | 0 | 0 | 1 |
| Total | | 1 | 0 | 6 |

Implemented functional endpoints: 1. To compute the percentage, divide by the number of planned endpoints.
