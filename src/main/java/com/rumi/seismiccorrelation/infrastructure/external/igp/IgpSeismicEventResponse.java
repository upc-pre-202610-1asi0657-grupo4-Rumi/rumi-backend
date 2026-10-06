package com.rumi.seismiccorrelation.infrastructure.external.igp;

import java.time.Instant;

public record IgpSeismicEventResponse(
        String code,
        double magnitude,
        Instant timestamp,
        double latitude,
        double longitude,
        double depthKm
) {
}
