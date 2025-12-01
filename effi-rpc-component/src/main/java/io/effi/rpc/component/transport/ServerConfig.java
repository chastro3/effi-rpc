package io.effi.rpc.component.transport;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.config.OptionName;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines configuration for server.
 */
@ScopedComponent(scope = PLATFORM)
public interface ServerConfig extends EndpointConfig {

    OptionName<Integer> ACCEPT_BACKLOG = OptionName.of("acceptBacklog", 1024);

    OptionName<Integer> ACCEPTOR_THREADS = OptionName.of("acceptorThreads", 1);

    OptionName<Integer> IO_THREADS = OptionName.of("ioThreads", Runtime.getRuntime().availableProcessors() * 2);

    interface Configurator<SELF extends Configurator<SELF>> extends EndpointConfig.Configurator<SELF> {

        /**
         * Set the accept backlog for the server.
         * <p>
         * Defines the maximum number of pending connections in the server's connection queue.
         * If the queue is full, the server may reject new connections.
         */
        default SELF acceptBacklog(int acceptBacklog) {
            addOption(ACCEPT_BACKLOG, acceptBacklog);
            return self();
        }

        /**
         * Set the number of threads for handling incoming connection requests.
         * <p>
         * The number of threads responsible for accepting new connections from clients.
         * You can adjust this number based on the expected volume of incoming connection requests.
         */
        default SELF acceptorThreads(int connectionHandlerThreads) {
            addOption(ACCEPTOR_THREADS, connectionHandlerThreads);
            return self();
        }

        /**
         * Set the number of threads for processing client requests.
         * <p>
         * The number of threads responsible for processing requests after the connection is accepted.
         * This can be adjusted based on the load and the expected traffic.
         */
        default SELF ioThreads(int requestProcessorThreads) {
            addOption(IO_THREADS, requestProcessorThreads);
            return self();
        }
    }
}

