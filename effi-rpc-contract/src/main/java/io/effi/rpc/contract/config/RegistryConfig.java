package io.effi.rpc.contract.config;

/**
 * Defines configuration for registry.
 */
public interface RegistryConfig extends NamedURLConfig {

    @Override
    default String repositoryKey() {
        return name();
    }
}
