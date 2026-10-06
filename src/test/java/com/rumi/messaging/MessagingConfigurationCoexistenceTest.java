package com.rumi.messaging;

import com.rumi.seismiccorrelation.infrastructure.messaging.rabbitmq.SeismicCorrelationMessagingConfiguration;
import com.rumi.structuralmonitoring.infrastructure.messaging.rabbitmq.StructuralMonitoringMessagingConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MessagingConfigurationCoexistenceTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(
                    StructuralMonitoringMessagingConfiguration.class,
                    SeismicCorrelationMessagingConfiguration.class
            );

    @Test
    void bothContextsShareOneMessageConverterWhileTheyLiveInTheMonolith() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(MessageConverter.class);
            assertThat(context.getBeansOfType(TopicExchange.class)).hasSize(2);
        });
    }
}
