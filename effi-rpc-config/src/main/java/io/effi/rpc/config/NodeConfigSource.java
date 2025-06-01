package io.effi.rpc.config;

import java.util.List;

/**
 * Provides access to a {@link NodeConfig} instance.
 */
public interface NodeConfigSource extends ConfigSource {

    @Override
    NodeConfig config();

    default List<String> getCascaded(ConfigKey configKey) {
        return config().getCascaded(configKey);
    }
}
