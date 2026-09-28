package io.effi.rpc.component.transport;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.transport.options.ClientOptions;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines configuration for client.
 */
@ScopedComponent(scope = PLATFORM)
public interface ClientConfig extends EndpointConfig {

    interface Configurator<SELF extends ServerConfig.Configurator<SELF>> extends EndpointConfig.Configurator<SELF> {

        default SELF maxConnections(int maxConnections) {
            addOption(ClientOptions.MAX_CONNECTIONS, maxConnections);
            return self();
        }

        default SELF maxPendingAcquires(int maxPendingAcquires) {
            addOption(ClientOptions.MAX_PENDING_ACQUIRES, maxPendingAcquires);
            return self();
        }

        default SELF acquireTimeout(int acquireTimeout) {
            addOption(ClientOptions.ACQUIRE_TIMEOUT, acquireTimeout);
            return self();
        }

        /**
         * Sets the connection timeout.
         */
        default SELF connectTimeout(int connectTimeout) {
            addOption(ClientOptions.CONNECT_TIMEOUT, connectTimeout);
            return self();
        }
    }
}


