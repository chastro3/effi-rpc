package io.effi.rpc.registry.nacos;

import io.effi.rpc.common.constant.Component;
import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.registry.AbstractRegistryFactory;
import io.effi.rpc.registry.RegistryService;

/**
 * {@link io.effi.rpc.registry.RegistryFactory} implementation based on nacos.
 */
@Extension(Component.Registry.NACOS)
public class NacosRegistryFactory extends AbstractRegistryFactory {
    @Override
    protected RegistryService create(EffRpcApplication application, URL url) {
        return new NacosRegistryService(application, url);
    }
}
