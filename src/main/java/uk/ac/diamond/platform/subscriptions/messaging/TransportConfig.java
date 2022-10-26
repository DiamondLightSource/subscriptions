package uk.ac.diamond.platform.subscriptions.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class TransportConfig implements WebSocketMessageBrokerConfigurer {
    private static final String SUBSCRIPTIONS_ENDPOINT = "/subscriptions";

    @Autowired
    WebSocketOutboundInterceptor channelOutboundInterceptor;

    @Autowired
    WebSocketInboundInterceptor channelInboundInterceptor;

    @Autowired
    private BrokerConfig broker;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(SUBSCRIPTIONS_ENDPOINT)
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay(broker.destinations())
                .setRelayHost(broker.host())
                .setRelayPort(broker.stompPort())
                .setClientLogin(broker.username())
                .setClientPasscode(broker.password())
                .setSystemLogin(broker.username())
                .setSystemPasscode(broker.password());

        registry.setApplicationDestinationPrefixes(broker.filteredDestinations());
    }

    public BrokerConfig getBrokerConfig() {
        return broker;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors((channelInboundInterceptor));
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(channelOutboundInterceptor);
    }
}

