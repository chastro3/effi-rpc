package io.effi.rpc.component.transport;

import io.effi.rpc.config.ConfigNames;

/**
 * Builds {@link ServerConfig} instance and defines configuration.
 */
public interface ServerConfigBuilder<T extends ServerConfig, SELF extends EndpointConfigBuilder<T, SELF>>
        extends EndpointConfigBuilder<T, SELF> {

    /**
     * Set the number of threads for handling incoming connection requests.
     * <p>
     * The number of threads responsible for accepting new connections from clients.
     * You can adjust this number based on the expected volume of incoming connection requests.
     */
    default SELF connectionHandlerThreads(int connectionHandlerThreads) {
        config().set(ConfigNames.CONNECTION_HANDLER_THREADS, connectionHandlerThreads);
        return self();
    }

    /**
     * Set the number of threads for processing client requests.
     * <p>
     * The number of threads responsible for processing requests after the connection is accepted.
     * This can be adjusted based on the load and the expected traffic.
     */
    default SELF requestProcessorThreads(int requestProcessorThreads) {
        config().set(ConfigNames.REQUEST_PROCESSOR_THREADS, requestProcessorThreads);
        return self();
    }

    /**
     * Set the accept backlog for the server.
     * <p>
     * Defines the maximum number of pending connections in the server's connection queue.
     * If the queue is full, the server may reject new connections.
     */
    default SELF acceptBacklog(int acceptBacklog) {
        config().set(ConfigNames.ACCEPT_BACKLOG, acceptBacklog);
        return self();
    }
}
