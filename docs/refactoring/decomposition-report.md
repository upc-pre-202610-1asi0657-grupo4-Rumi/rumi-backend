# Decomposition Report

How the `rumi-backend` modular monolith was prepared to be split into one service per bounded context.
Baseline: tag `monolith-baseline` (`bf8bc87`). Work branch: `feature/decompose-by-bounded-context`.

## 1. Decomposition strategies applied

| Strategy | How it was applied here |
|---|---|
| Decompose by business capability / DDD bounded context | Service boundaries follow the bounded contexts of the domain model: Building Management, Structural Monitoring and Seismic Correlation. The risk index is a capability of Seismic Correlation, so the standalone `riskassessment` package was merged into it. |
| Database per service | Each context has its own database: `buildingsdb`, `sensorreadingsdb` (TimescaleDB) and `seismiceventsdb`. No tables are shared and other contexts are referenced by id only. See `database-per-context.md`. |
| Event-driven decoupling | Structural Monitoring and Seismic Correlation communicate only through the `SensorReadingRecorded` event over RabbitMQ. Each side owns its copy of the contract and its messaging configuration. See `event-contracts.md`. |
| Anti-corruption layer | Seismic Correlation no longer imports Structural Monitoring's domain event; it translates the incoming message into its own `SensorReadingRecorded` type in its infrastructure layer. The existing `IgpSeismicEventMapper` plays the same role towards the external IGP feed. |
| Incremental extraction (strangler fig style) | The refactoring was done in small steps inside the running monolith, with the test suite green after every commit. The monolith stays in place as the legacy system while each context is extracted to its own repository. |

No port/adapter pair had to be introduced for a synchronous call: the coupling report found no context
calling another context's services directly.

## 2. Before and after

### Before (`monolith-baseline`)

```
com.rumi
├── BuildingManagementServiceApplication
├── buildingmanagement      (application, domain, infrastructure)
├── structuralmonitoring    (domain.event, infrastructure.messaging)
├── seismiccorrelation      (application, domain, infrastructure)  --> imports structuralmonitoring, shared
├── riskassessment          (domain only)
└── shared.infrastructure.messaging.RabbitMqConfiguration          <-- imported by structuralmonitoring, seismiccorrelation
```

- 5 cross-context imports (see `coupling-report.md`).
- One event class and one RabbitMQ configuration shared by two contexts.
- One datasource, `rumi_building_management`, for the whole application.
- REST path `/api/buildings`, IDE files under version control.

### After (`feature/decompose-by-bounded-context`)

```
com.rumi
├── RumiApplication
├── buildingmanagement      (application, domain, infrastructure)
├── structuralmonitoring    (domain.event, infrastructure.messaging + own messaging configuration)
└── seismiccorrelation      (application, domain.event, domain.model, domain.service, domain.strategy,
                             infrastructure + own messaging configuration)
```

- 0 cross-context imports in `src/main`: every context compiles against its own packages only.
- `shared` and `riskassessment` no longer exist. No class or test was deleted; they were moved with `git mv`.
- The event contract is duplicated per context and documented.
- One database per context.
- REST path `/api/v1/buildings`, matching the API gateway routes.
- Test suite: 8 tests before, 9 after (one test added to check that both messaging configurations can coexist in the monolith).

The only test that references two contexts is `com.rumi.messaging.MessagingConfigurationCoexistenceTest`.
It belongs to the monolith and is not carried into any extracted service.

## 3. Refactoring commits

| Hash | Message |
|---|---|
| `3b28143` | `docs(refactoring): add coupling report and endpoint inventory` |
| `8a6816d` | `chore(repo): stop tracking ide files` |
| `5088b36` | `refactor(app): rename main class to RumiApplication` |
| `8bfc808` | `refactor(buildingmanagement): version rest api path as /api/v1` |
| `b9caa9b` | `refactor(seismiccorrelation): merge riskassessment into seismic correlation` |
| `213972e` | `refactor(seismiccorrelation): decouple from other contexts` |
| `3ace5b4` | `refactor(messaging): move messaging into the contexts that use it` |
| `a0ab764` | `refactor(persistence): configure one database per context` |

## 4. Target services

| Context | Repository | Port |
|---|---|---|
| Building Management | `rumi-building-service` | 8081 |
| Structural Monitoring | `rumi-monitoring-service` | 8082 |
| Seismic Correlation (with the risk index) | `rumi-seismic-service` | 8083 |

Each service repository is extracted from this branch with its history preserved, so the commits listed above
remain visible in the history of the service they touched.
