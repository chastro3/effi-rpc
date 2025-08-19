package io.effi.rpc.component.transport;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.config.ConfigValues;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines configuration for client.
 */
@ScopedComponent(scope = PLATFORM)
public interface ClientConfig extends EndpointConfig {

    static String defaultKey(String protocol) {
        return protocol + "-" + ConfigValues.DEFAULT;
    }
}


