package uk.ac.diamond.platform.subscriptions.websockets;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class OutboundInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        final StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        String mess = accessor.getFirstNativeHeader(StompHeaderAccessor.STOMP_MESSAGE_HEADER);
        StompCommand command = accessor.getCommand();
        switch (command) {
            case ERROR -> log.error("STOMP protocol error, connection will be closed:", mess);
            default -> log.debug("STOMP {} command message for session {} with payload {}",
                    command, accessor.getSessionId(), new String((byte[]) message.getPayload(), StandardCharsets.UTF_8));
        }
        return message;
    }
}
