package io.effi.rpc.contract.config;

import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;

import java.net.InetSocketAddress;

/**
 * Defines configuration for server.
 */
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

