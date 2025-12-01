package io.effi.rpc.component.transport;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.config.OptionName;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines configuration for client.
 */
@ScopedComponent(scope = PLATFORM)
public interface ClientConfig extends EndpointConfig {

    OptionName<Integer> MAX_CONNECTIONS = OptionName.of("maxConnections",3);

    OptionName<Integer> CONNECT_TIMEOUT = OptionName.of("connectTimeout",3000);

    interface Configurator<SELF extends ServerConfig.Configurator<SELF>> extends EndpointConfig.Configurator<SELF> {

        default SELF maxConnections(int maxConnections) {
            addOption(MAX_CONNECTIONS, maxConnections);
            return self();
        }

        /**
         * Sets the connection timeout.
         */
        default SELF connectTimeout(int connectTimeout) {
            addOption(CONNECT_TIMEOUT, connectTimeout);
            return self();
        }
    }
}


