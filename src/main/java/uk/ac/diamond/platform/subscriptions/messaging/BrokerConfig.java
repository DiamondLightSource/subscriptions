package uk.ac.diamond.platform.subscriptions.messaging;


public interface BrokerConfig {
        String host();
        int port();
        int stompPort();
        String username();
        String password();
        String[] destinations();
        String[] filteredDestinations();
        }