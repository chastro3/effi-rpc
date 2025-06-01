package io.effi.rpc.boot.registry;

import io.effi.rpc.constant.Component;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.registry.MetaDataRegister;
import io.effi.rpc.annotation.spi.Extension;

import java.util.Map;

/**
 * Register default meta data to registry.
 */
@Extension(Component.DEFAULT)
public class DefaultMetaDataRegister implements MetaDataRegister {

    @Override
    public void process(ServiceHost serviceHost, final Map<String, String> metaData) {
        DefaultRegistryMetaData defaultRegistryMetaData = new DefaultRegistryMetaData(serviceHost);
        metaData.putAll(defaultRegistryMetaData.toMap());
        metaData.put(KeyConstant.PROTOCOL, serviceHost.url().protocol());
        metaData.putAll(serviceHost.url().params().items());
    }
}
