package io.effi.rpc.registry.nacos;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.registry.AbstractRegistryClientFactory;
import io.effi.rpc.registry.RegistryClient;

import static io.effi.rpc.registry.nacos.NacosRegistryClientFactory.NAME;

/**
 * Implements {@link RegistryClient.Factory} using Nacos.
 */
@Extension(NAME)
public class NacosRegistryClientFactory extends AbstractRegistryClientFactory {

    public static final String NAME = "nacos";

    @Override
    protected RegistryClient newClient(RegistryConfig config, ScopedPlatform platform) {
        return new NacosRegistryClient(config, platform);
    }
}
