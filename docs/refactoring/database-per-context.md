# Database per Context

Each bounded context owns its data store. No context reads or writes another context's tables;
data owned by another context is referenced by identifier only (for example `buildingId`).

| Context | Target service | Database | Engine | Properties | Environment variables | Tables today |
|---|---|---|---|---|---|---|
| Building Management | `rumi-building-service` | `buildingsdb` | PostgreSQL | `spring.datasource.*` | `BUILDING_DB_URL`, `BUILDING_DB_USERNAME`, `BUILDING_DB_PASSWORD` | `buildings` |
| Structural Monitoring | `rumi-monitoring-service` | `sensorreadingsdb` | TimescaleDB on PostgreSQL | `rumi.datasource.monitoring.*` | `MONITORING_DB_URL`, `MONITORING_DB_USERNAME`, `MONITORING_DB_PASSWORD` | none yet |
| Seismic Correlation | `rumi-seismic-service` | `seismiceventsdb` | PostgreSQL | `rumi.datasource.seismic.*` | `SEISMIC_DB_URL`, `SEISMIC_DB_USERNAME`, `SEISMIC_DB_PASSWORD` | none yet |

## Current state

- Building Management is the only context with JPA entities, so it is the only one bound to a live
  datasource (`spring.datasource`). Its default database changed from `rumi_building_management`
  to `buildingsdb`.
- Structural Monitoring and Seismic Correlation have no entities or repositories yet. Their
  connection settings are declared under `rumi.datasource.<context>` and no connection is opened
  for them. When each context is extracted, these settings become that service's own datasource.
- There were no foreign keys or joins between contexts, so no table had to be split.
- There are no Spring profiles in the project; none was added or removed.

## Local setup

```sql
CREATE DATABASE buildingsdb;
CREATE DATABASE sensorreadingsdb;
CREATE DATABASE seismiceventsdb;

-- inside buildingsdb (hibernate.ddl-auto is "validate", the table must exist)
CREATE TABLE buildings (
    id      uuid PRIMARY KEY,
    name    varchar(255) NOT NULL,
    address varchar(255) NOT NULL
);
```
