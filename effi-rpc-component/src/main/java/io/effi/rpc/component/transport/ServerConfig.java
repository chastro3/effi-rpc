package io.effi.rpc.component.transport;

import io.effi.rpc.annotation.component.ScopedComponent;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines configuration for server.
 */
@ScopedComponent(scope = PLATFORM)
public interface ServerConfig extends EndpointConfig {
}

