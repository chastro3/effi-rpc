package io.effi.rpc.config.transport;

import io.effi.rpc.config.DefaultConfigNames;

/**
 * Builds {@link ServerConfig} instance and defines configuration.
 */
public interface ServerConfigBuilder<T extends ServerConfig, C extends EndpointConfigBuilder<T, C>>
        extends EndpointConfigBuilder<T, C> {

    /**
     * Set the number of threads for handling incoming connection requests.
     * <p>
     * The number of threads responsible for accepting new connections from clients.
     * You can adjust this number based on the expected volume of incoming connection requests.
     */
    default C connectionHandlerThreads(int connectionHandlerThreads) {
        config().set(DefaultConfigNames.CONNECTION_HANDLER_THREADS, connectionHandlerThreads);
        return returnThis();
    }

    /**
     * Set the number of threads for processing client requests.
     * <p>
     * The number of threads responsible for processing requests after the connection is accepted.
     * This can be adjusted based on the load and the expected traffic.
     */
    default C requestProcessorThreads(int requestProcessorThreads) {
        config().set(DefaultConfigNames.REQUEST_PROCESSOR_THREADS, requestProcessorThreads);
        return returnThis();
    }

    /**
     * Set the accept backlog for the server.
     * <p>
     * Defines the maximum number of pending connections in the server's connection queue.
     * If the queue is full, the server may reject new connections.
     */
    default C acceptBacklog(int acceptBacklog) {
        config().set(DefaultConfigNames.ACCEPT_BACKLOG, acceptBacklog);
        return returnThis();
    }
}
