package uk.ac.diamond.platform.subscriptions.websockets;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.ac.diamond.platform.subscriptions.config.BrokerConnection;

/**
 * Intercepts messages from the Web Socket channel before they are forwarded on to the broker
 */
@Slf4j
@Service
public class InboundInterceptor implements ChannelInterceptor {
    @Autowired
    private BrokerConnection connection;

    /**
     * Intercepts messages from inbound websocket channel before they are fowarded on to the broker. The messages are
     * validated according to the STOMP 1.2 spec and set to null if they fail, which will result in the message not
     * being forwarded.
     *
     * @param message   The message to be examined
     * @param channel   Descriptor of the channel we are connected to
     * @return          The message, if validation succeeds (with a possibly modified destination), or null
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        log.trace("Message Headers: {}", message.getHeaders().values());

        final StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
        StompCommand command = acc.getCommand();
        if (command != null) {
            switch (command) {
                case CONNECT, STOMP -> {
                    if (StringUtils.hasText(acc.getFirstNativeHeader("accept-version"))
                                                    && StringUtils.hasText(acc.getHost())) {
                        log.info("New connection created {}", sessionDetail(acc));
                    } else {
                        message = null;
                        log.error("Invalid connect request: " +
                                "Connect messages must contain both accept-version and host headers");
                    }
                }
                case SUBSCRIBE -> {
                    if (StringUtils.hasText(acc.getFirstNativeHeader("id"))
                                                    && StringUtils.hasText(acc.getReceipt())) {
                        message = validateDestination(message, acc);
                    } else {
                        message = null;
                        log.error("Invalid subscribe request for {} {}: " +
                                "Subscribe messages must contain both subscription id and receipt id headers",
                                acc.getDestination(), sessionDetail(acc));
                    }
                }
                case UNSUBSCRIBE -> {
                    if (StringUtils.hasText(acc.getFirstNativeHeader("id"))) {
                        log.info("Deleting subscription with id {} {}", acc.getSubscriptionId(), sessionDetail(acc));
                    } else {
                        message = null;
                        log.warn("Cannot unsubscribe, no subscription id header supplied");
                    }
                }
                case DISCONNECT -> log.info("Disconnected {}", sessionDetail(acc));
                default -> log.debug("STOMP {} command message with payload {}", command, acc.getMessage());
            }
        }
        return message;
    }

    /**
     * Checks the existence and validity of the destination header for SUBSCRIBE messages, prepending the routing prefix
     * if it is not present. Invalid or missing destinations result in a null message being returned which will cause
     * Spring to abort the attempt to create the subscription with the Broker. Because SUBSCRIBE messages are required
     * to have a receipt request set, this will allow clients to detect an unsuccessful subscription attempt if the
     * receipt is not forthcoming within their chosen timeout period.
     *
     * @param message   The incoming message from a client
     * @param acc       The StompHeaderAccessor associated with  the message
     * @return          The validation and potentially formatted message or null
     */
    private Message<?> validateDestination(Message<?> message, StompHeaderAccessor acc) {
        String destination = acc.getDestination();

        if (StringUtils.hasText(destination)) {
            if(!destination.startsWith(connection.routingPrefix())) {
                destination = connection.routingPrefix() + destination;
            }

            if (!destination.startsWith(connection.destinations()[0])) {
                log.error("Only public topics may be subscribed to; {} is not public", destination);
                message = null;
            } else {
                acc.setDestination(destination);
                message = MessageBuilder.createMessage(message.getPayload(), acc.getMessageHeaders());
                log.info("Creating subscription to {} for session {} with subscriptionId {} and receiptId {}",
                        destination, acc.getSessionId(), acc.getSubscriptionId(), acc.getReceipt());
            }
        } else {
            log.error("NULL or Empty destination. Subscribe messages must specify a valid destination topic");
            message = null;
        }
        return message;
    }

    private String sessionDetail(StompHeaderAccessor acc) {
        return StringUtils.hasText(acc.getSessionId()) ? "for session " + acc.getSessionId() : "";
    }

}
