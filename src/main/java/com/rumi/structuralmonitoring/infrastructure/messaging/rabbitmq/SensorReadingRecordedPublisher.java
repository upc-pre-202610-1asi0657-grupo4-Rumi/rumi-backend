package com.rumi.structuralmonitoring.infrastructure.messaging.rabbitmq;

import com.rumi.structuralmonitoring.domain.event.SensorReadingRecorded;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingRecordedPublisher {

    private final RabbitTemplate rabbitTemplate;

    public SensorReadingRecordedPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(SensorReadingRecorded event) {
        rabbitTemplate.convertAndSend(
                StructuralMonitoringMessagingConfiguration.SENSOR_READING_EXCHANGE,
                StructuralMonitoringMessagingConfiguration.SENSOR_READING_ROUTING_KEY,
                event
        );
    }
}
