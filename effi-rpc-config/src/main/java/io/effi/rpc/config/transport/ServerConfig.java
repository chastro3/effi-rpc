package io.effi.rpc.config.transport;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;

import java.net.InetSocketAddress;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines configuration for server.
 */
@ScopedComponent(scope = PLATFORM)
public interface ServerConfig extends EndpointConfig {

    @Override
    default URL newUrl(InetSocketAddress address) {
        return URL.builder()
                .type(URLType.SERVER)
                .protocol(protocol())
                .address(address)
                .params(config().items())
                .build();
    }
}

