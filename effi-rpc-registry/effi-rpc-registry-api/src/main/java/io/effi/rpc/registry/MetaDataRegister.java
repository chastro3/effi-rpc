package io.effi.rpc.registry;

import io.effi.rpc.common.extension.spi.Extensible;
import io.effi.rpc.common.url.URL;

import java.util.Map;

/**
 * Handles registration of metadata for services.
 * <p>Allows for extension of registered metadata upon service signup.</p>
 */
@Extensible(lazyLoad = false)
public interface MetaDataRegister {

    /**
     * Processes metadata before the service is registered.
     *
     * @param url      the URL of the service being registered
     * @param metaData the metadata associated with the service
     */
    void process(URL url, Map<String, String> metaData);
}

