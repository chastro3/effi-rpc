package io.effi.rpc.config;

import java.util.List;

/**
 * Represents a hierarchical configuration with parent support.
 */
public interface NodeConfig extends Config {

    /**
     * Retrieves cascaded values for the given config key.
     */
    List<String> getCascaded(ConfigName key);

    /**
     * Retrieves cascaded values for the given string key.
     */
    List<String> getCascaded(String key);

    /**
     * Returns the parent configuration.
     */
    Config parent();

    /**
     * Provides access to the {@link NodeConfig}.
     */
    interface Provider extends Config.Provider {

        default List<String> getCascadedConfig(ConfigName configName) {
            return config().getCascaded(configName);
        }

        @Override
        NodeConfig config();
    }
}


