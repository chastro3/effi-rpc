package io.effi.rpc.contract.config;

/**
 * Configuration for registry.
 *
 * @see NamedURLConfig
 * @see io.effi.rpc.support.builder.RegistryConfigBuilder
 */
public interface RegistryConfig extends NamedURLConfig {
    @Override
    default String managerKey() {
        return name();
    }
}
