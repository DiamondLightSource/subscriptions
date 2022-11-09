package uk.ac.diamond.platform.subscriptions.websockets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Service;
import uk.ac.diamond.platform.subscriptions.config.BrokerConnection;

/**
 * Intercepts messages from the Web Socket channel before they are forwarded on to the broker
 */
@Slf4j
@Service
public class InboundInterceptor implements ChannelInterceptor {
    @Autowired
    private BrokerConnection connection;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.trace("Message Headers: {}", message.getHeaders().values());

        final StompHeaderAccessor acc = (StompHeaderAccessor) StompHeaderAccessor.getMutableAccessor(message);
        StompCommand command = acc.getCommand();
        if (command != null) {
            switch (command) {
                case CONNECT -> log.info("New connection for session {}", acc.getSessionId());
                case SUBSCRIBE -> {
                    String destination = formattedDestination(acc);
                    if (!destination.startsWith(connection.destinations()[0])) {
                        log.error("Only public topics may be subscribed to; {} is not public", destination);
                        message = null;
                    } else {
                        log.info("Creating subscription to {} for session {} with subscriptionId {} and receiptId {}",
                                destination, acc.getSessionId(), acc.getSubscriptionId(), acc.getReceipt());
                    }
                }
                case UNSUBSCRIBE -> log.info("Deleting subscription to {} for session {} with id {}",
                        acc.getDestination(), acc.getSessionId(), acc.getSubscriptionId());
                case DISCONNECT -> log.info("Session {} disconnected", acc.getSessionId());
                default -> log.debug("STOMP {} command message with payload {}", command, acc.getMessage());
            }
        }
        return message;
    }

    private String formattedDestination(StompHeaderAccessor acc) {
        String formatted = "";

        String destination = acc.getDestination();
        if (destination != null) {
            if(!destination.startsWith(connection.routingPrefix())) {
                acc.setDestination(connection.routingPrefix() + destination);
            }
            formatted = acc.getDestination();
        }
        acc.setImmutable();
        return formatted;
    }
}
