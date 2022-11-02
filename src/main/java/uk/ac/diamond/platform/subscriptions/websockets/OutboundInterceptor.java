package uk.ac.diamond.platform.subscriptions.websockets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * Intercepts messages from the broker before they are forwarded on to the Web Socket channel
 */
@Slf4j
@Service
public class OutboundInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.trace("Message Headers: {}", message.getHeaders().values());

        final StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
        StompCommand command = acc.getCommand();
        if (command != null) {
            switch (command) {
                case RECEIPT -> log.info("Receipt returned for id {}", acc.getReceiptId());
                case ERROR -> log.error("STOMP protocol error, connection will be closed: {}", acc.getMessage());
                default -> log.debug("STOMP {} command message for session {} with headers{} payload {}",
                        command, acc.getSessionId(), acc.getMessageHeaders().values(), new String((byte[]) message.getPayload(), StandardCharsets.UTF_8));
            }
        }
        return message;
    }
}
