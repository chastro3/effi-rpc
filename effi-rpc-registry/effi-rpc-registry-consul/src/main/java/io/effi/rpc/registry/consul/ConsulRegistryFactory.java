package io.effi.rpc.registry.consul;

import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.registry.AbstractRegistryFactory;
import io.effi.rpc.registry.RegistryService;

import static io.effi.rpc.common.constant.Component.Registry.CONSUL;

/**
 * {@link io.effi.rpc.registry.RegistryFactory} implementation based on consul.
 */
@Extension(CONSUL)
public class ConsulRegistryFactory extends AbstractRegistryFactory {

    @Override
    protected RegistryService create(EffRpcApplication application, URL url) {
        return new ConsulRegistryService(application, url);
    }

}

