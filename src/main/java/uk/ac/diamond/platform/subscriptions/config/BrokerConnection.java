package uk.ac.diamond.platform.subscriptions.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;


@ConfigurationProperties(prefix = "connection")
@ConstructorBinding
public record BrokerConnection(
        String host,
        int port,
        String username,
        String password,
        String[] destinations,
        String[] filteredDestinations,
        String routingPrefix) {

}