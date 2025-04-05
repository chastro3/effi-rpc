package io.effi.rpc.common.config;

import java.util.List;

/**
 * Provides access to a {@link LinkedConfig} instance.
 */
public interface LinkedConfigSource extends ConfigSource {

    @Override
    LinkedConfig config();

    default List<String> getCascaded(ConfigKey configKey) {
        return config().getCascaded(configKey);
    }
}
