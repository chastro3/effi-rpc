package io.effi.rpc.engine.builder;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.contract.config.RegistryConfig;

/**
 * Builds {@link RegistryConfig} instances and defines configuration for registry.
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
        config.set(DefaultConfigKeys.CONNECT_TIMEOUT.key(), String.valueOf(connectTimeout));
        return returnThis();
    }

    /**
     * Sets the number of retry attempts.
     */
    public C retries(int retries) {
        config.set(DefaultConfigKeys.RETRIES.key(), String.valueOf(retries));
        return returnThis();
    }

    /**
     * Sets the health check interval.
     */
    public C healthCheckInterval(int healthCheckInterval) {
        config.set(DefaultConfigKeys.HEALTH_CHECK_INTERVAL.key(), String.valueOf(healthCheckInterval));
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


