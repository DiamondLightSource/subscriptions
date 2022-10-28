package uk.ac.diamond.platform.subscriptions.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "websocket")
@ConstructorBinding
public record Websocket(String subscriptionEndpoint) {
}
