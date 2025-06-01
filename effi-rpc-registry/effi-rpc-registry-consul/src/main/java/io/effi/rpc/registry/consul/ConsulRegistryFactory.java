package io.effi.rpc.registry.consul;

import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.registry.AbstractRegistryFactory;
import io.effi.rpc.registry.RegistryService;
import io.effi.rpc.annotation.spi.Extension;

import static io.effi.rpc.constant.Component.Registry.CONSUL;

/**
 * Implements {@link io.effi.rpc.registry.RegistryFactory} using Consul.
 */
@Extension(CONSUL)
public class ConsulRegistryFactory extends AbstractRegistryFactory {

    @Override
    protected RegistryService newService(RegistryConfig config) {
        return new ConsulRegistryService(config);
    }

}

