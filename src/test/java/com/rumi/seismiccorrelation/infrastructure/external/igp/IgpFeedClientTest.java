package com.rumi.seismiccorrelation.infrastructure.external.igp;

import com.rumi.seismiccorrelation.domain.model.SeismicEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class IgpFeedClientTest {

    @Test
    void convertsAnIgpResponseToTheRumiDomainModel() {
        IgpFeedClient client = new IgpFeedClient(new IgpSeismicEventMapper());
        Instant timestamp = Instant.parse("2026-10-06T14:25:00Z");
        IgpSeismicEventResponse response = new IgpSeismicEventResponse(
                "IGP-2026-001",
                5.4,
                timestamp,
                -12.0464,
                -77.0428,
                38.0
        );

        SeismicEvent event = client.processResponse(response);

        assertThat(event.getId()).isEqualTo("IGP-2026-001");
        assertThat(event.getMagnitude()).isEqualTo(5.4);
        assertThat(event.getOccurredAt()).isEqualTo(timestamp);
        assertThat(event.getLatitude()).isEqualTo(-12.0464);
        assertThat(event.getLongitude()).isEqualTo(-77.0428);
        assertThat(event.getDepth()).isEqualTo(38.0);
    }
}
