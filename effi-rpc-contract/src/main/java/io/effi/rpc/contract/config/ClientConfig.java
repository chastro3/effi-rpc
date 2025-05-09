package io.effi.rpc.contract.config;

import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;

import java.net.InetSocketAddress;

/**
 * Defines configuration for client.
 */
public interface ClientConfig extends EndpointConfig {

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


