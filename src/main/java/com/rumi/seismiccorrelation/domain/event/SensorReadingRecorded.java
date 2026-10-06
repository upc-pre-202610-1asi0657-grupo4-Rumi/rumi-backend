package com.rumi.seismiccorrelation.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Seismic Correlation's own copy of the event contract published by Structural Monitoring.
 * The other context is referenced by identifiers only.
 */
public record SensorReadingRecorded(
        UUID eventId,
        UUID sensorId,
        UUID buildingId,
        Instant recordedAt,
        double value
) {
    public SensorReadingRecorded {
        Objects.requireNonNull(eventId, "Event id is required");
        Objects.requireNonNull(sensorId, "Sensor id is required");
        Objects.requireNonNull(buildingId, "Building id is required");
        Objects.requireNonNull(recordedAt, "Recording time is required");
    }
}
