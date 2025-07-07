package io.effi.rpc.config.v2;


import java.util.List;

/**
 * Represents a hierarchical configuration with parent support.
 */
public interface NodeConfig extends Config {

    /**
     * Retrieves cascaded values for the given config key.
     */
    <V> List<V> getCascaded(ConfigName<V> name);


    /**
     * Returns the parent configuration.
     */
    Config parent();

    /**
     * Provides access to the {@link NodeConfig}.
     */
    interface Provider extends Config.Provider {

        default <V> List<V> getCascadedConfig(ConfigName<V> name) {
            return config().getCascaded(name);
        }

        @Override
        NodeConfig config();
    }
}


