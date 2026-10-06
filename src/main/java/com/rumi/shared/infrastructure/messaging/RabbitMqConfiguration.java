package com.rumi.shared.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfiguration {

    public static final String SENSOR_READING_EXCHANGE = "rumi.structural-monitoring.events";
    public static final String SENSOR_READING_QUEUE = "rumi.seismic-correlation.sensor-reading-recorded";
    public static final String SENSOR_READING_ROUTING_KEY = "structural-monitoring.sensor-reading.recorded";

    @Bean
    public TopicExchange sensorReadingExchange() {
        return new TopicExchange(SENSOR_READING_EXCHANGE, true, false);
    }

    @Bean
    public Queue sensorReadingQueue() {
        return QueueBuilder.durable(SENSOR_READING_QUEUE).build();
    }

    @Bean
    public Binding sensorReadingBinding(Queue sensorReadingQueue, TopicExchange sensorReadingExchange) {
        return BindingBuilder.bind(sensorReadingQueue)
                .to(sensorReadingExchange)
                .with(SENSOR_READING_ROUTING_KEY);
    }

    @Bean
    public MessageConverter rabbitMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
