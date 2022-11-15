package uk.ac.diamond.platform.subscriptions.websockets;

import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.*;

public class MessageHelper {

    private final long[] defaultHeartbeat = new long[]{0, 0};

    protected Map<String, Object> headers = new HashMap<>();
    protected byte[] payload = {};

    protected MultiValueMap<String, String> nativeHeaders = new LinkedMultiValueMap<>();

    protected String routingPrefix = "/topic/";
    protected String publicRoot = "public.";
    protected String destinations = routingPrefix + publicRoot;
    protected String defaultDestination = "some.destination";
    protected String defaultId = "abc123";

    protected MessageHelper(){
        headers.put("nativeHeaders", nativeHeaders);
    }

    protected MessageHelper withAccept(String... versions) {
       nativeHeaders.add(StompHeaders.ACCEPT_VERSION,
               versions != null && versions.length == 1 ? versions[0] : "any");
       return this;
    }

    protected MessageHelper withHost(String... hosts) {
        nativeHeaders.add(StompHeaders.HOST,
                hosts != null && hosts.length == 1 ? hosts[0] : "/");
        return this;
    }

    protected MessageHelper withDestination(@NonNull String destination) {
        headers.put(SimpMessageHeaderAccessor.DESTINATION_HEADER, destination);
        nativeHeaders.add(StompHeaders.DESTINATION, destination);
        return this;
    }

    protected MessageHelper withReceipt(@NonNull String reciptId) {
        nativeHeaders.add(StompHeaders.RECEIPT, reciptId);
        return this;
    }

    protected MessageHelper withSubscriptionId(@NonNull String subId) {
        nativeHeaders.add(StompHeaders.ID, subId );
        headers.put(SimpMessageHeaderAccessor.SUBSCRIPTION_ID_HEADER, subId);
        return this;
    }

    protected MessageHelper withHeartbeat(long... timeouts) {
        long[] toSet = timeouts != null && timeouts.length > 0
                ? new long[]{timeouts[0], timeouts[1]}
                : defaultHeartbeat;

        nativeHeaders.add(StompHeaders.HEARTBEAT, Arrays.toString(toSet));
        headers.put(SimpMessageHeaderAccessor.HEART_BEAT_HEADER, toSet);
        return this;
    }

    protected MessageHelper withStompCommand(@NonNull StompCommand command){
        headers.put("stompCommand", command);
        headers.put(SimpMessageHeaderAccessor.MESSAGE_TYPE_HEADER, command.getMessageType());
        return this;
    }

    protected Message<?> build() {
        return  MessageBuilder.createMessage(payload, new MessageHeaders(headers));
    }
}
