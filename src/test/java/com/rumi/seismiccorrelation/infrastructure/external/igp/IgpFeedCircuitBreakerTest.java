package com.rumi.seismiccorrelation.infrastructure.external.igp;

import com.rumi.seismiccorrelation.domain.model.SeismicEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = IgpFeedCircuitBreakerTest.TestApplication.class,
        properties = {
                "spring.autoconfigure.exclude="
                        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                        + "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
        }
)
class IgpFeedCircuitBreakerTest {

    @Autowired
    private IgpFeedClient client;

    @Test
    void returnsTheMappedEventWhenTheIgpOperationSucceeds() {
        IgpSeismicEventResponse response = new IgpSeismicEventResponse(
                "IGP-2026-002",
                4.8,
                Instant.parse("2026-10-06T16:00:00Z"),
                -11.9,
                -77.1,
                42.0
        );

        Optional<SeismicEvent> result = client.fetchLatestEvent(() -> response);

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().getId()).isEqualTo("IGP-2026-002");
    }

    @Test
    void returnsNoEventWhenTheIgpOperationFails() {
        Optional<SeismicEvent> result = client.fetchLatestEvent(() -> {
            throw new IllegalStateException("IGP service unavailable");
        });

        assertThat(result).isEmpty();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {

        @Bean
        IgpSeismicEventMapper igpSeismicEventMapper() {
            return new IgpSeismicEventMapper();
        }

        @Bean
        IgpFeedClient igpFeedClient(IgpSeismicEventMapper mapper) {
            return new IgpFeedClient(mapper);
        }
    }
}
