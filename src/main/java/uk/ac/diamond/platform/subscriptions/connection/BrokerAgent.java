package uk.ac.diamond.platform.subscriptions.connection;

import java.util.Set;

public interface BrokerAgent {

    Set<String> getCurrentTopicNames();
    Set<String> getCurrentQueueNames();
}
