package uk.ac.diamond.platform.subscriptions.websockets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import uk.ac.diamond.platform.subscriptions.config.BrokerConnection;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class InboundInterceptorTest extends MessageHelper {

    Message<?> message;

    MessageChannel channel = (message, timeout) -> false;

    @Spy
    BrokerConnection connection = new BrokerConnection(
            "", 0, "", "",
            new String[]{destinations},
            null,
            routingPrefix);

    @InjectMocks
    InboundInterceptor interceptor;


    @Test
    void connectMessagesWithoutAcceptVersionAreRejected() {
        this.withStompCommand(StompCommand.CONNECT)
                .withHost();
        message = this.withStompCommand(StompCommand.CONNECT)
                .withHost()
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void connectMessagesWithoutHostAreRejected() {
        message = this.withStompCommand(StompCommand.CONNECT)
                .withAccept()
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void connectMessagesWithAcceptVersionAndHostAreAccepted() {
        message = this.withStompCommand(StompCommand.CONNECT)
                .withAccept()
                .withHost()
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNotNull(outboundMessage);
    }

    @Test
    void stompMessagesWithoutAcceptVersionAreRejected() {
        this.withStompCommand(StompCommand.STOMP)
                .withHost();
        message = this.withStompCommand(StompCommand.CONNECT)
                .withHost()
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void stompMessagesWithoutHostAreRejected() {
        message = this.withStompCommand(StompCommand.STOMP)
                .withAccept()
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void stompMessagesWithAcceptVersionAndHostAreAccepted() {
        message = this.withStompCommand(StompCommand.STOMP)
                .withAccept()
                .withHost()
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNotNull(outboundMessage);
    }

    @Test
    void subscribeMessagesWithoutDestinationdAreRejected() {
        message = this.withStompCommand(StompCommand.SUBSCRIBE)
                .withReceipt(defaultId)
                .withSubscriptionId(defaultId)
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void subscribeMessagesWithoutSubscriptionIdAreRejected() {
        message = this.withStompCommand(StompCommand.SUBSCRIBE)
                .withDestination(defaultDestination)
                .withReceipt(defaultId)
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void subscribeMessagesWithoutReceiptIdAreRejected() {
        message = this.withStompCommand(StompCommand.SUBSCRIBE)
                .withDestination(defaultDestination)
                .withSubscriptionId(defaultId)
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void subscribeMessagesWithReceiptIdAndSubscriptionIdButNullDestinationAreRejected() {
        message = this.withStompCommand(StompCommand.SUBSCRIBE)
                .withReceipt(defaultId)
                .withSubscriptionId(defaultId)
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void subscribeMessagesWithReceiptIdAndSubscriptionIdButInvalidDestinationAreRejected() {
        message = this.withStompCommand(StompCommand.SUBSCRIBE)
                .withDestination(defaultDestination)
                .withReceipt(defaultId)
                .withSubscriptionId(defaultId)
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void subscribeMessagesWithReceiptIdAndSubscriptionIdButPrefixedInvalidDestinationAreRejected() {
        message = this.withStompCommand(StompCommand.SUBSCRIBE)
                .withDestination(routingPrefix + "pubtic." + defaultDestination)
                .withReceipt(defaultId)
                .withSubscriptionId(defaultId)
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void subscribeMessagesWithValidHeadersAndValidDestinationAreAcceptedAndDestinationIsPrefixed() {
        message = this.withStompCommand(StompCommand.SUBSCRIBE)
                .withDestination(publicRoot + defaultDestination)
                .withReceipt(defaultId)
                .withSubscriptionId(defaultId)
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNotNull(outboundMessage);
        assertTrue(Objects.requireNonNull(
                outboundMessage.getHeaders().get(SimpMessageHeaderAccessor.DESTINATION_HEADER)).toString()
                .startsWith(destinations));
    }

    @Test
    void subscribeMessagesWithValidHeadersAndPrefixedValidDestinationAreAcceptedAndDestinationIsUnchanged() {
        message = this.withStompCommand(StompCommand.SUBSCRIBE)
                .withDestination(routingPrefix + publicRoot + defaultDestination)
                .withReceipt(defaultId)
                .withSubscriptionId(defaultId)
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNotNull(outboundMessage);
        assertTrue(Objects.requireNonNull(
                outboundMessage.getHeaders().get(SimpMessageHeaderAccessor.DESTINATION_HEADER)).toString()
                .startsWith(destinations));
    }

    @Test
    void unsubscribeMessagesWithNoSubscriptionIdAreRejected() {
        message = this.withStompCommand(StompCommand.UNSUBSCRIBE)
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNull(outboundMessage);
    }

    @Test
    void unsubscribeMessagesWithSubscriptionIdAreAccepted() {
        message = this.withStompCommand(StompCommand.UNSUBSCRIBE)
                .withSubscriptionId(defaultId)
                .build();

        Message<?> outboundMessage = interceptor.preSend(message, channel);
        assertNotNull(outboundMessage);
    }
}