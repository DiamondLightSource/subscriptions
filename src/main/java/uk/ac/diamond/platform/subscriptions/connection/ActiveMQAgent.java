package uk.ac.diamond.platform.subscriptions.connection;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.DestinationEvent;
import org.apache.activemq.advisory.DestinationListener;
import org.apache.activemq.advisory.DestinationSource;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.ac.diamond.platform.subscriptions.messaging.BrokerConfig;

import javax.jms.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Profile("activemq")
@Slf4j
public class ActiveMQAgent implements BrokerAgent {
    private static final int TOPIC_EVENT = 2;
    private static final int ADD_TOPIC_MAX_DELAY = 10000000;  // 10ms

    private DestinationSource destinationSource;
    private Listener listener = new Listener();
    private Set<ActiveMQTopic> topics;
    private Instant start;


    @Autowired
    public ActiveMQAgent(BrokerConfig broker) {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
        connectionFactory.setBrokerURL("tcp://" + broker.host() + ":" + broker.port());
        try(Connection connection = connectionFactory.createConnection(broker.username(), broker.password())) {
            connection.start();

            destinationSource = ((ActiveMQConnection) connection).getDestinationSource();
            destinationSource.setDestinationListener(listener);
            start = Instant.now();
            while (Duration.between(start, Instant.now()).getNano() < ADD_TOPIC_MAX_DELAY);
            topics = destinationSource.getTopics();
            log.info("After Listener: {}", topics.size());
        } catch(Exception e) {

        }
    }

    public Set<String> getCurrentTopicNames() {
        return collectNames(topics);
    }

    public Set<String> getCurrentQueueNames() {
        return collectNames(destinationSource.getQueues());
    }

    private Set<String> collectNames(Set<? extends ActiveMQDestination> destinations) {
        return destinations.stream()
                .map(ActiveMQDestination::getPhysicalName)
                .collect(Collectors.toSet());
    }

    private class Listener implements DestinationListener {
        @Override
        public void onDestinationEvent(DestinationEvent destinationEvent) {
            if (destinationEvent.getDestination().getDestinationType() == TOPIC_EVENT) {
                String name = destinationEvent.getDestination().getPhysicalName();
                if (destinationEvent.isAddOperation()) {
                    start = Instant.now();
                    log.info("Added {}:", name);
                } else if (destinationEvent.isRemoveOperation()) {
                    log.info("Removed {}, size: ", name);
                }
            }
        }
    }
}
