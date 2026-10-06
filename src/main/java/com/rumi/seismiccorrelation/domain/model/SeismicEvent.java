package com.rumi.seismiccorrelation.domain.model;

import java.time.Instant;
import java.util.Objects;

public final class SeismicEvent {

    private final String id;
    private final double magnitude;
    private final Instant occurredAt;
    private final double latitude;
    private final double longitude;
    private final double depth;

    public SeismicEvent(
            String id,
            double magnitude,
            Instant occurredAt,
            double latitude,
            double longitude,
            double depth
    ) {
        this.id = Objects.requireNonNull(id, "Seismic event id is required");
        this.magnitude = magnitude;
        this.occurredAt = Objects.requireNonNull(occurredAt, "Occurrence time is required");
        this.latitude = latitude;
        this.longitude = longitude;
        this.depth = depth;
    }

    public String getId() {
        return id;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDepth() {
        return depth;
    }
}
