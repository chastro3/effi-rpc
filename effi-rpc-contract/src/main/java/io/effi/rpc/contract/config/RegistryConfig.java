package io.effi.rpc.contract.config;

/**
 * Configuration for registry.
 *
 * @see NamedURLConfig
 * @see io.effi.rpc.engine.builder.RegistryConfigBuilder
 */
public interface RegistryConfig extends NamedURLConfig {
    @Override
    default String repositoryKey() {
        return name();
    }
}
