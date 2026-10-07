package com.rumi.structuralmonitoring.infrastructure.messaging.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StructuralMonitoringMessagingConfiguration {

    public static final String SENSOR_READING_EXCHANGE = "rumi.structural-monitoring.events";
    public static final String SENSOR_READING_ROUTING_KEY = "structural-monitoring.sensor-reading.recorded";

    @Bean
    public TopicExchange structuralMonitoringSensorReadingExchange() {
        return new TopicExchange(SENSOR_READING_EXCHANGE, true, false);
    }

    // Only one converter may exist per application; the other context declares the same guard.
    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    public MessageConverter structuralMonitoringMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
