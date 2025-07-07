package io.effi.rpc.config.registry;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.NamedConfigBuilder;
import io.effi.rpc.config.URL;

/**
 * Builds {@link RegistryConfig} instance and defines configuration.
 */
public abstract class RegistryConfigBuilder<T extends RegistryConfig, C extends RegistryConfigBuilder<T, C>>
        extends NamedConfigBuilder<T, C> {

    protected String address;

    /**
     * Sets the registry URL and extracts type and address.
     */
    public C url(String url) {
        URL urlObj = URL.valueOf(url);
        protocol = urlObj.protocol();
        address = urlObj.address();
        config.set(urlObj.params().items());
        return returnThis();
    }

    /**
     * Sets the connection timeout.
     */
    public C connectTimeout(int connectTimeout) {
        config.set(DefaultConfigNames.CONNECT_TIMEOUT, connectTimeout);
        return returnThis();
    }

    /**
     * Sets the number of retry attempts.
     */
    public C retries(int retries) {
        config.set(DefaultConfigNames.RETRIES, retries);
        return returnThis();
    }

    /**
     * Sets the heartbeat send interval.
     */
    public C heartbeatInterval(int heartbeatInterval) {
        config.set(DefaultConfigNames.HEARTBEAT_INTERVAL, heartbeatInterval);
        return returnThis();
    }

    @Override
    protected T build(Config config) {
        URL url = URL.builder()
                .protocol(protocol)
                .address(address)
                .params(config().items())
                .build();
        if (name == null) {
            name = url.uri();
        }
        return build(url);
    }

    protected abstract T build(URL url);
}


