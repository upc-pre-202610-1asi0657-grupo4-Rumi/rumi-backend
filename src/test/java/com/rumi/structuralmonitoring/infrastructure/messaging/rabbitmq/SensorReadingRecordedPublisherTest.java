package com.rumi.structuralmonitoring.infrastructure.messaging.rabbitmq;

import com.rumi.shared.infrastructure.messaging.RabbitMqConfiguration;
import com.rumi.structuralmonitoring.domain.event.SensorReadingRecorded;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SensorReadingRecordedPublisherTest {

    @Test
    void publishesTheEventUsingTheConfiguredExchangeAndRoutingKey() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        SensorReadingRecordedPublisher publisher = new SensorReadingRecordedPublisher(rabbitTemplate);
        SensorReadingRecorded event = new SensorReadingRecorded(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-10-06T15:30:00Z"),
                0.018
        );

        publisher.publish(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMqConfiguration.SENSOR_READING_EXCHANGE,
                RabbitMqConfiguration.SENSOR_READING_ROUTING_KEY,
                event
        );
    }
}
