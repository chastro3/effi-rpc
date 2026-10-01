package io.effi.rpc.component.transport;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.transport.options.ServerOptions;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines server endpoint configuration.
 */
@ScopedComponent(scope = PLATFORM)
public interface ServerConfig extends EndpointConfig {

    interface Configurator<SELF extends Configurator<SELF>> extends EndpointConfig.Configurator<SELF> {

        /**
         * Sets the maximum number of pending accepted connections.
         *
         * @param acceptBacklog accept backlog size
         * @return this configurator
         */
        default SELF acceptBacklog(int acceptBacklog) {
            addOption(ServerOptions.ACCEPT_BACKLOG, acceptBacklog);
            return self();
        }

        /**
         * Sets the number of acceptor threads.
         *
         * @param connectionHandlerThreads acceptor thread count
         * @return this configurator
         */
        default SELF acceptorThreads(int connectionHandlerThreads) {
            addOption(ServerOptions.ACCEPTOR_THREADS, connectionHandlerThreads);
            return self();
        }

        /**
         * Sets the number of request-processing I/O threads.
         *
         * @param requestProcessorThreads request-processing thread count
         * @return this configurator
         */
        default SELF ioThreads(int requestProcessorThreads) {
            addOption(ServerOptions.IO_THREADS, requestProcessorThreads);
            return self();
        }
    }
}

