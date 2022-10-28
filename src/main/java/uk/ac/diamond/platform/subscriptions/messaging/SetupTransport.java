package uk.ac.diamond.platform.subscriptions.messaging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import uk.ac.diamond.platform.subscriptions.config.BrokerConnection;
import uk.ac.diamond.platform.subscriptions.config.Websocket;
import uk.ac.diamond.platform.subscriptions.websockets.InboundInterceptor;
import uk.ac.diamond.platform.subscriptions.websockets.OutboundInterceptor;

/**
 * Registers the websocket subscription endpoint, sets up the relay to the active message broker and registers the
 * websocket interceptors to facilitate logging of the STOMP messages as they pass through.
 */
@Configuration
@EnableWebSocketMessageBroker
public class SetupTransport implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private OutboundInterceptor channelOutboundInterceptor;

    @Autowired
    private InboundInterceptor channelInboundInterceptor;

    @Autowired
    private BrokerConnection brokerConnection;

    @Autowired
    private Websocket websocket;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(websocket.subscriptionEndpoint())
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableStompBrokerRelay(brokerConnection.destinations())
                .setRelayHost(brokerConnection.host())
                .setRelayPort(brokerConnection.port())
                .setClientLogin(brokerConnection.username())
                .setClientPasscode(brokerConnection.password())
                .setSystemLogin(brokerConnection.username())
                .setSystemPasscode(brokerConnection.password());

        registry.setApplicationDestinationPrefixes(brokerConnection.filteredDestinations());
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

