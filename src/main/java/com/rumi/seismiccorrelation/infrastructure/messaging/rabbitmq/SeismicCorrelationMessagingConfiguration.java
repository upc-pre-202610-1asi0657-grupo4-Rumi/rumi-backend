package com.rumi.seismiccorrelation.infrastructure.messaging.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class SeismicCorrelationMessagingConfiguration {

    public static final String SENSOR_READING_EXCHANGE = "rumi.structural-monitoring.events";
    public static final String SENSOR_READING_QUEUE = "rumi.seismic-correlation.sensor-reading-recorded";
    public static final String SENSOR_READING_ROUTING_KEY = "structural-monitoring.sensor-reading.recorded";

    @Bean
    public TopicExchange seismicCorrelationSensorReadingExchange() {
        return new TopicExchange(SENSOR_READING_EXCHANGE, true, false);
    }

    @Bean
    public Queue seismicCorrelationSensorReadingQueue() {
        return QueueBuilder.durable(SENSOR_READING_QUEUE).build();
    }

    @Bean
    public Binding seismicCorrelationSensorReadingBinding(
            Queue seismicCorrelationSensorReadingQueue,
            TopicExchange seismicCorrelationSensorReadingExchange
    ) {
        return BindingBuilder.bind(seismicCorrelationSensorReadingQueue)
                .to(seismicCorrelationSensorReadingExchange)
                .with(SENSOR_READING_ROUTING_KEY);
    }

    // Only one converter may exist per application; the other context declares the same guard.
    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter seismicCorrelationMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
