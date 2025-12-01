package io.effi.rpc.registry.consul;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.registry.AbstractRegistryClientFactory;
import io.effi.rpc.registry.RegistryClient;

import static io.effi.rpc.registry.consul.ConsulRegistryClientFactory.NAME;

/**
 * Implements {@link RegistryClient.Factory} using Consul.
 */
@Extension(NAME)
public class ConsulRegistryClientFactory extends AbstractRegistryClientFactory {

    public static final String NAME = "consul";

    @Override
    protected RegistryClient newClient(RegistryConfig config, ScopedPlatform platform) {
        return new ConsulRegistryClient(config, platform);
    }

}

