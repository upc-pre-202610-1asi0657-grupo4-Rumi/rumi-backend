# Coupling Report: `rumi-backend` modular monolith

Read-only analysis of the monolith at commit `bf8bc87` (`main`, tag `monolith-baseline` once Phase B starts).
It is the input for the decomposition into one repository per bounded context.

- Build: Maven, Spring Boot parent `3.5.6`, Java `21`, single `pom.xml` (artifactId `building-management-service`).
- Main class: `com.rumi.BuildingManagementServiceApplication`.
- Size: 25 main classes, 6 test classes (8 test methods), 848 lines of Java.

## 1. Package inventory

| Package | Layer | Classes |
|---|---|---|
| `com.rumi` | bootstrap | `BuildingManagementServiceApplication` |
| `com.rumi.buildingmanagement.application` | application | `BuildingApplicationService` |
| `com.rumi.buildingmanagement.domain.model` | domain | `Building` |
| `com.rumi.buildingmanagement.domain.repository` | domain | `BuildingRepository` (port) |
| `com.rumi.buildingmanagement.infrastructure.persistence` | infrastructure | `BuildingJpaEntity`, `JpaBuildingRepository`, `SpringDataBuildingRepository` |
| `com.rumi.buildingmanagement.infrastructure.web` | infrastructure | `BuildingController`, `BuildingDtoMapper` |
| `com.rumi.buildingmanagement.infrastructure.web.dto` | infrastructure | `BuildingRequestDto`, `BuildingResponseDto` |
| `com.rumi.structuralmonitoring.domain.event` | domain | `SensorReadingRecorded` |
| `com.rumi.structuralmonitoring.infrastructure.messaging.rabbitmq` | infrastructure | `SensorReadingRecordedPublisher` |
| `com.rumi.seismiccorrelation.application` | application | `SensorReadingRecordedHandler` |
| `com.rumi.seismiccorrelation.domain.model` | domain | `SeismicEvent` |
| `com.rumi.seismiccorrelation.infrastructure.external.igp` | infrastructure | `IgpFeedClient`, `IgpFeedOperation`, `IgpSeismicEventMapper`, `IgpSeismicEventResponse` |
| `com.rumi.seismiccorrelation.infrastructure.messaging.rabbitmq` | infrastructure | `SensorReadingRecordedSubscriber` |
| `com.rumi.riskassessment.domain.service` | domain | `RiskIndexCalculationService` |
| `com.rumi.riskassessment.domain.strategy` | domain | `RiskCalculationStrategy`, `AiRiskStrategy`, `RuleBasedRiskStrategy` |
| `com.rumi.shared.infrastructure.messaging` | shared | `RabbitMqConfiguration` |

Classes per context: building management 10, structural monitoring 2, seismic correlation 6, risk assessment 4, shared 1, bootstrap 1.

## 2. Cross-context imports

Every `import com.rumi.<other-context>...` found in the code base.

| # | From (context / class) | To (context / class) | Kind | Source set |
|---|---|---|---|---|
| 1 | seismiccorrelation / `application.SensorReadingRecordedHandler` | structuralmonitoring / `domain.event.SensorReadingRecorded` | direct use of another context's domain event class | main |
| 2 | seismiccorrelation / `infrastructure.messaging.rabbitmq.SensorReadingRecordedSubscriber` | structuralmonitoring / `domain.event.SensorReadingRecorded` | direct use of another context's domain event class | main |
| 3 | seismiccorrelation / `infrastructure.messaging.rabbitmq.SensorReadingRecordedSubscriber` | shared / `infrastructure.messaging.RabbitMqConfiguration` (`SENSOR_READING_QUEUE`) | shared kernel constant | main |
| 4 | structuralmonitoring / `infrastructure.messaging.rabbitmq.SensorReadingRecordedPublisher` | shared / `infrastructure.messaging.RabbitMqConfiguration` (`SENSOR_READING_EXCHANGE`, `SENSOR_READING_ROUTING_KEY`) | shared kernel constant | main |
| 5 | structuralmonitoring / `SensorReadingRecordedPublisherTest` | shared / `infrastructure.messaging.RabbitMqConfiguration` | shared kernel constant | test |

Dependency graph (arrows mean "imports"):

```
buildingmanagement      (no outgoing, no incoming)
riskassessment          (no outgoing, no incoming)
seismiccorrelation ---> structuralmonitoring   (SensorReadingRecorded)
seismiccorrelation ---> shared                 (RabbitMqConfiguration)
structuralmonitoring -> shared                 (RabbitMqConfiguration)
```

Findings:

- `buildingmanagement` is already fully isolated: it imports no other context and nothing imports it.
- `riskassessment` is isolated and has only a domain layer. It is not wired as a Spring bean and has no caller in `src/main`.
- The only real context-to-context coupling is `seismiccorrelation -> structuralmonitoring` through the event class `SensorReadingRecorded`.
- `shared` is a shared kernel holding the whole RabbitMQ topology for both sides.
- There are no synchronous calls between contexts (no service of one context injected into another), so no port/adapter anti-corruption layer is needed for an existing call.

## 3. Shared classes

| Class | Used by | What is shared |
|---|---|---|
| `shared.infrastructure.messaging.RabbitMqConfiguration` | structuralmonitoring (publisher), seismiccorrelation (subscriber) | exchange/queue/routing-key constants, `TopicExchange`, `Queue`, `Binding` and `MessageConverter` beans |
| `structuralmonitoring.domain.event.SensorReadingRecorded` | structuralmonitoring (publisher), seismiccorrelation (handler, subscriber) | event contract class used on both sides of the broker |

## 4. Events

| Event | Published by | Consumed by | Exchange (type) | Routing key | Queue |
|---|---|---|---|---|---|
| `SensorReadingRecorded` | structuralmonitoring `SensorReadingRecordedPublisher#publish` | seismiccorrelation `SensorReadingRecordedSubscriber#onSensorReadingRecorded` -> `SensorReadingRecordedHandler#handle` | `rumi.structural-monitoring.events` (topic, durable) | `structural-monitoring.sensor-reading.recorded` | `rumi.seismic-correlation.sensor-reading-recorded` (durable) |

Payload (JSON through `Jackson2JsonMessageConverter`):

| Field | Java type | Required |
|---|---|---|
| `eventId` | `UUID` | yes |
| `sensorId` | `UUID` | yes |
| `buildingId` | `UUID` | yes |
| `recordedAt` | `Instant` | yes |
| `value` | `double` | yes (primitive) |

Observations:

- `SensorReadingRecordedPublisher#publish` has no caller in `src/main`; nothing produces the event yet.
- `SensorReadingRecordedHandler#handle` only logs the event id.
- The converter writes a `__TypeId__` header with the publisher's fully qualified class name. Once the event class is duplicated per context the subscriber must resolve the type from the listener method parameter (the Spring AMQP default, inferred type precedence), not from that header.

## 5. Configuration properties per context

Single `src/main/resources/application.yml`, no profiles.

| Property | Value | Context that needs it |
|---|---|---|
| `spring.application.name` | `building-management-service` | whole monolith |
| `spring.datasource.url` | `${BUILDING_DB_URL:jdbc:postgresql://localhost:5432/rumi_building_management}` | buildingmanagement |
| `spring.datasource.username` | `${BUILDING_DB_USERNAME:postgres}` | buildingmanagement |
| `spring.datasource.password` | `${BUILDING_DB_PASSWORD:postgres}` | buildingmanagement |
| `spring.jpa.hibernate.ddl-auto` | `validate` | buildingmanagement |
| `spring.jpa.open-in-view` | `false` | buildingmanagement |
| `resilience4j.circuitbreaker.instances.igpFeed.*` | count based, window 4, min calls 2, threshold 50, open 10s | seismiccorrelation |
| `spring.rabbitmq.*` | not set (Spring Boot defaults: `localhost:5672`, `guest`/`guest`) | structuralmonitoring, seismiccorrelation |
| `server.port` | not set (default `8080`) | whole monolith |

Maven dependencies per context:

| Dependency | buildingmanagement | structuralmonitoring | seismiccorrelation (+ risk) |
|---|---|---|---|
| `spring-boot-starter-web` | yes | no | no |
| `spring-boot-starter-data-jpa` + `postgresql` | yes | no | no |
| `spring-boot-starter-amqp` | no | yes | yes |
| `spring-boot-starter-json` | yes | yes | yes |
| `spring-boot-starter-aop` + `resilience4j-spring-boot3` 2.3.0 | no | no | yes |
| `spring-boot-starter-test` | yes | yes | yes |

## 6. JPA entities and tables per context

| Context | Entity | Table | Columns |
|---|---|---|---|
| buildingmanagement | `BuildingJpaEntity` | `buildings` | `id` (uuid, PK), `name` (not null), `address` (not null) |
| structuralmonitoring | none | none | none |
| seismiccorrelation | none | none | none |
| riskassessment | none | none | none |

- There are no relationships or foreign keys between contexts, so there is no table to split.
- There are no migration scripts (no Flyway/Liquibase, no `schema.sql`); `ddl-auto: validate` expects the table to exist already.

## 7. Tests per package

| Test class | Package | Methods | Type | Cross-context import |
|---|---|---|---|---|
| `BuildingApplicationServiceTest` | `buildingmanagement.application` | 1 | plain unit | none |
| `BuildingDtoMapperTest` | `buildingmanagement.infrastructure.web` | 1 | plain unit | none |
| `RiskIndexCalculationServiceTest` | `riskassessment.domain.service` | 2 | plain unit | none |
| `IgpFeedClientTest` | `seismiccorrelation.infrastructure.external.igp` | 1 | plain unit | none |
| `IgpFeedCircuitBreakerTest` | `seismiccorrelation.infrastructure.external.igp` | 2 | `@SpringBootTest` with its own `TestApplication`; excludes DataSource, JPA and RabbitMQ auto-configuration | none |
| `SensorReadingRecordedPublisherTest` | `structuralmonitoring.infrastructure.messaging.rabbitmq` | 1 | Mockito unit | `shared` (`RabbitMqConfiguration`) |

Total: 6 classes, 8 test methods. No test needs a database or a broker.

## 8. Other observations (nothing was changed or deleted)

- `.idea/` is tracked (4 files: `.gitignore`, `Rumi.iml`, `modules.xml`, `vcs.xml`).
- The Maven artifactId and `spring.application.name` are `building-management-service` although the project contains four contexts.
- `BuildingRepository#existsById` and `BuildingApplicationService#findBuildingById` have no caller in `src/main` (the latter is used by a test).
- The only controller is mapped under `/api/buildings`, without the `/v1` segment used by the target gateway routes.
- `AiRiskStrategy` and `RuleBasedRiskStrategy` contain the same private `validate` method.
