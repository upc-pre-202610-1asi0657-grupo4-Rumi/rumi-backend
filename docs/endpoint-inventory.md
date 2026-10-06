# Endpoint Inventory

One row per REST endpoint that exists in the code base at commit `bf8bc87`.
Status values: `implemented` (controller, application service and persistence wired end to end), `partial` (exposed but incomplete).

## Existing endpoints

| # | Target service | Verb | Path | Controller#method | Request DTO | Response DTO | User story | Status |
|---|---|---|---|---|---|---|---|---|
| 1 | `rumi-building-service` | POST | `/api/buildings` | `BuildingController#registerBuilding` | `BuildingRequestDto` (`name`, `address`) | `BuildingResponseDto` (`id`, `name`, `address`), HTTP 201 | US04 register building | implemented |

Notes on endpoint 1:

- The path has no `/v1` segment; the target gateway route for this service is `/api/v1/buildings/**`.
- There is no bean validation and no exception handler: a blank `name` or `address` raises `IllegalArgumentException` in the `Building` constructor and is returned as HTTP 500.

## Contexts without REST endpoints

| Context | Target service | REST controllers | What exists instead |
|---|---|---|---|
| structuralmonitoring | `rumi-monitoring-service` | none | `SensorReadingRecordedPublisher` (RabbitMQ) |
| seismiccorrelation | `rumi-seismic-service` | none | `SensorReadingRecordedSubscriber` (RabbitMQ), `IgpFeedClient` (US13, not exposed over REST) |
| riskassessment | `rumi-seismic-service` | none | `RiskIndexCalculationService` and strategies (US11, not exposed over REST) |

## Application-layer operations not exposed over REST

| Context | Operation | Possible endpoint | User story |
|---|---|---|---|
| buildingmanagement | `BuildingApplicationService#findBuildingById` | `GET /api/v1/buildings/{buildingId}` | US04 |

## Summary

| Target service | Implemented | Partial |
|---|---|---|
| `rumi-building-service` | 1 | 0 |
| `rumi-monitoring-service` | 0 | 0 |
| `rumi-seismic-service` | 0 | 0 |
| Total | 1 | 0 |
