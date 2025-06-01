package io.effi.rpc.config;

/**
 * Provides access to a {@link Config} instance.
 */
public interface ConfigSource {

    /**
     * Returns the associated {@link Config}.
     */
    Config config();

    default String get(ConfigKey key) {
        return config().get(key);
    }
}
