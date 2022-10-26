package uk.ac.diamond.platform.subscriptions.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WebSocketInboundInterceptor implements ChannelInterceptor {

    private static final String PUBLIC_TOPIC_PREFIX = "/topic/public";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        StompCommand command = accessor.getCommand();
        switch (command) {
            case CONNECT -> log.info("New connection for session {}", accessor.getSessionId());
            case SUBSCRIBE -> {
                String destination = accessor.getDestination();
                if (!destination.startsWith(PUBLIC_TOPIC_PREFIX)) {
                    log.error("Only public topics may be subscribed to; {} is not public", destination);
                    message = null;
                } else {
                    log.info("Creating subscription to {} for session {} with id {}",
                            destination, accessor.getSessionId(), accessor.getSubscriptionId());
                }
            }
            case UNSUBSCRIBE -> log.info("Deleting subscription to {} for session {} with id {}",
                    accessor.getDestination(), accessor.getSessionId(), accessor.getSubscriptionId());
            case DISCONNECT -> log.info("Session {} disconnected", accessor.getSessionId());
            default -> log.debug("STOMP {} command message with payload {}", command, accessor.getMessage());
        }
        return message;
    }

}
