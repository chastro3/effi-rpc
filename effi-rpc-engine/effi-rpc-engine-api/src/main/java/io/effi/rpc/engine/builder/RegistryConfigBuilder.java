package io.effi.rpc.engine.builder;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.config.RegistryConfig;

/**
 * Builder for creating {@link RegistryConfig} instances,defining settings for registry.
 *
 * @param <T> The type of {@link RegistryConfig}.
 * @param <C> The type of the builder.
 */
public abstract class RegistryConfigBuilder<T extends RegistryConfig, C extends RegistryConfigBuilder<T, C>>
        extends NamedConfigBuilder<T, C> {

    /**
     * Registry address.
     */
    protected String address;

    /**
     * Sets the registry URL and extracts type and address.
     */
    public C url(String url) {
        URL urlObj = URL.valueOf(url);
        protocol = urlObj.protocol();
        address = urlObj.address();
        config.set(urlObj.params().properties());
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
                .params(config().properties())
                .build();
        if (name == null) {
            name = url.uri();
        }
        return build(url);
    }

    protected abstract T build(URL url);
}


