package io.effi.rpc.engine.registry;

import io.effi.rpc.constant.Component;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.spi.Extension;
import io.effi.rpc.config.URL;
import io.effi.rpc.registry.MetaDataRegister;

import java.util.Map;

/**
 * Register default meta data to registry.
 */
@Extension(Component.DEFAULT)
public class DefaultMetaDataRegister implements MetaDataRegister {

    @Override
    public void process(URL url, final Map<String, String> metaData) {
        DefaultRegistryMetaData defaultRegistryMetaData = new DefaultRegistryMetaData(url);
        metaData.putAll(defaultRegistryMetaData.toMap());
        metaData.put(KeyConstant.PROTOCOL, url.protocol());
        metaData.putAll(url.params().items());
    }
}
