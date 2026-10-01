package io.effi.rpc.component.transport;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.transport.options.ClientOptions;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines client endpoint configuration.
 */
@ScopedComponent(scope = PLATFORM)
public interface ClientConfig extends EndpointConfig {

    interface Configurator<SELF extends ServerConfig.Configurator<SELF>> extends EndpointConfig.Configurator<SELF> {

        /**
         * Sets the maximum number of pooled connections.
         *
         * @param maxConnections maximum pooled connections
         * @return this configurator
         */
        default SELF maxConnections(int maxConnections) {
            addOption(ClientOptions.MAX_CONNECTIONS, maxConnections);
            return self();
        }

        /**
         * Sets the maximum number of pending connection acquires.
         *
         * @param maxPendingAcquires maximum pending acquires
         * @return this configurator
         */
        default SELF maxPendingAcquires(int maxPendingAcquires) {
            addOption(ClientOptions.MAX_PENDING_ACQUIRES, maxPendingAcquires);
            return self();
        }

        /**
         * Sets the connection acquire timeout.
         *
         * @param acquireTimeout acquire timeout in milliseconds
         * @return this configurator
         */
        default SELF acquireTimeout(int acquireTimeout) {
            addOption(ClientOptions.ACQUIRE_TIMEOUT, acquireTimeout);
            return self();
        }

        /**
         * Sets the connection timeout.
         *
         * @param connectTimeout connection timeout in milliseconds
         * @return this configurator
         */
        default SELF connectTimeout(int connectTimeout) {
            addOption(ClientOptions.CONNECT_TIMEOUT, connectTimeout);
            return self();
        }
    }
}


