package io.effi.rpc.registry.consul;

import io.effi.rpc.config.URL;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.registry.AbstractRegistryFactory;
import io.effi.rpc.registry.RegistryService;
import io.effi.rpc.spi.Extension;

import static io.effi.rpc.constant.Component.Registry.CONSUL;

/**
 * Implements {@link io.effi.rpc.registry.RegistryFactory} using Consul.
 */
@Extension(CONSUL)
public class ConsulRegistryFactory extends AbstractRegistryFactory {

    @Override
    protected RegistryService create(EffRpcApplication application, URL url) {
        return new ConsulRegistryService(application, url);
    }

}

