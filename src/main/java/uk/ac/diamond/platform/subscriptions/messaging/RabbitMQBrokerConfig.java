package uk.ac.diamond.platform.subscriptions.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.context.annotation.Profile;

@Profile("rabbitmq")
@ConfigurationProperties(prefix = "rabbitmq")
@ConstructorBinding
public record RabbitMQBrokerConfig(
        String host,
        int port,
        int stompPort,
        String username,
        String password,
        String[] destinations,
        String[] filteredDestinations) implements BrokerConfig {

}