package io.effi.rpc.registry.consul;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.registry.AbstractRegistryClientFactory;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.RegistryClientFactory;

import static io.effi.rpc.config.ConfigValues.Registry.CONSUL;

/**
 * Implements {@link RegistryClientFactory} using Consul.
 */
@Extension(CONSUL)
public class ConsulRegistryClientFactory extends AbstractRegistryClientFactory {

    @Override
    protected RegistryClient newClient(RegistryConfig config, ScopedPlatform platform) {
        return new ConsulRegistryClient(config, platform);
    }

}

