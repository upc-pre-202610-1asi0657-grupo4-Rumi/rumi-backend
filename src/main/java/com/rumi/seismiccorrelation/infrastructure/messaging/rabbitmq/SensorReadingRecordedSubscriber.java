package com.rumi.seismiccorrelation.infrastructure.messaging.rabbitmq;

import com.rumi.seismiccorrelation.application.SensorReadingRecordedHandler;
import com.rumi.shared.infrastructure.messaging.RabbitMqConfiguration;
import com.rumi.seismiccorrelation.domain.event.SensorReadingRecorded;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SensorReadingRecordedSubscriber {

    private final SensorReadingRecordedHandler handler;

    public SensorReadingRecordedSubscriber(SensorReadingRecordedHandler handler) {
        this.handler = handler;
    }

    @RabbitListener(queues = RabbitMqConfiguration.SENSOR_READING_QUEUE)
    public void onSensorReadingRecorded(SensorReadingRecorded event) {
        handler.handle(event);
    }
}
