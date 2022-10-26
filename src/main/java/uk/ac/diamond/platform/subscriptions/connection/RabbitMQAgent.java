package uk.ac.diamond.platform.subscriptions.connection;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Profile("rabbitmq")
public class RabbitMQAgent implements BrokerAgent {
    @Override
    public Set<String> getCurrentTopicNames() {
        return null;
    }

    @Override
    public Set<String> getCurrentQueueNames() {
        return null;
    }
}
