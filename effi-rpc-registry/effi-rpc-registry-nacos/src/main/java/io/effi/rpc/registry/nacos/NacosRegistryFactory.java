package io.effi.rpc.registry.nacos;

import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.constant.Component;
import io.effi.rpc.registry.AbstractRegistryFactory;
import io.effi.rpc.registry.RegistryService;
import io.effi.rpc.annotation.spi.Extension;

/**
 * Implements {@link io.effi.rpc.registry.RegistryFactory} using Nacos.
 */
@Extension(Component.Registry.NACOS)
public class NacosRegistryFactory extends AbstractRegistryFactory {
    @Override
    protected RegistryService newService(RegistryConfig config) {
        return new NacosRegistryService(config);
    }
}
