# Event Contracts

Bounded contexts exchange information asynchronously through RabbitMQ.
There is no shared module: each context owns its copy of the contract class and its own messaging configuration, so every service can be built and deployed on its own.
A contract change must be applied on both sides.

## SensorReadingRecorded

A sensor reading was stored by Structural Monitoring.

| Item | Value |
|---|---|
| Publisher | Structural Monitoring (`rumi-monitoring-service`) |
| Consumer | Seismic Correlation (`rumi-seismic-service`) |
| Exchange | `rumi.structural-monitoring.events` (topic, durable, not auto-delete) |
| Routing key | `structural-monitoring.sensor-reading.recorded` |
| Queue | `rumi.seismic-correlation.sensor-reading-recorded` (durable), owned and declared by the consumer |
| Content type | `application/json` (`Jackson2JsonMessageConverter`) |

### Payload

| Field | JSON type | Format | Required | Description |
|---|---|---|---|---|
| `eventId` | string | UUID | yes | Unique id of this event |
| `sensorId` | string | UUID | yes | Sensor that produced the reading |
| `buildingId` | string | UUID | yes | Building where the sensor is installed |
| `recordedAt` | string | ISO-8601 instant (UTC) | yes | Moment the reading was taken |
| `value` | number | double | yes | Measured value |

```json
{
  "eventId": "7c1f5b0e-3a52-4d0b-9f5e-2f6c1a8f4d11",
  "sensorId": "b2a9d0f4-6c1e-4a57-8f0a-91d2c3e4f5a6",
  "buildingId": "3f2c8a10-5d7b-4e9a-b1c2-0a1b2c3d4e5f",
  "recordedAt": "2026-10-06T15:30:00Z",
  "value": 0.018
}
```

### Classes

| Side | Contract class | Messaging configuration | Entry point |
|---|---|---|---|
| Publisher | `com.rumi.structuralmonitoring.domain.event.SensorReadingRecorded` | `com.rumi.structuralmonitoring.infrastructure.messaging.rabbitmq.StructuralMonitoringMessagingConfiguration` (declares the exchange) | `SensorReadingRecordedPublisher#publish` |
| Consumer | `com.rumi.seismiccorrelation.domain.event.SensorReadingRecorded` | `com.rumi.seismiccorrelation.infrastructure.messaging.rabbitmq.SeismicCorrelationMessagingConfiguration` (declares the exchange, the queue and the binding) | `SensorReadingRecordedSubscriber#onSensorReadingRecorded` |

### Notes

- Both sides declare the exchange with identical arguments, so either service can start first.
- The publisher adds a `__TypeId__` header with its own class name. The consumer ignores it: the target type is inferred from the `@RabbitListener` method parameter, so the two classes only have to agree on the JSON fields.
- The consumer references the building and the sensor by identifier only; it holds no object of the publishing context.
- While both contexts still run inside the monolith, their configurations register a single `MessageConverter` (`@ConditionalOnMissingBean`). Each extracted service registers its own.
