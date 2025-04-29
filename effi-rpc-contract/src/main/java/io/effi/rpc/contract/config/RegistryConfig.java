package io.effi.rpc.contract.config;

/**
 * Configuration for registry.
 */
public interface RegistryConfig extends NamedURLConfig {
    @Override
    default String repositoryKey() {
        return name();
    }
}
