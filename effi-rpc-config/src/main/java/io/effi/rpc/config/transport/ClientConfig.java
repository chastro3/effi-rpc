package io.effi.rpc.config.transport;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;
import io.effi.rpc.constant.Component;

import java.net.InetSocketAddress;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines configuration for client.
 */
@ScopedComponent(scope = PLATFORM)
public interface ClientConfig extends EndpointConfig {

    static String defaultKey(String protocol) {
        return protocol + "-" + Component.DEFAULT;
    }

    @Override
    default URL newUrl(InetSocketAddress address) {
        return URL.builder()
                .type(URLType.CLIENT)
                .protocol(protocol())
                .address(address)
                .params(config().items())
                .build();
    }

}


