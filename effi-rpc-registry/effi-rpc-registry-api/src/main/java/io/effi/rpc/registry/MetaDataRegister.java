package io.effi.rpc.registry;

import io.effi.rpc.config.URL;
import io.effi.rpc.spi.Extensible;

import java.util.Map;

/**
 * Registers and extends service metadata during registration.
 */
@Extensible(lazyLoad = false)
public interface MetaDataRegister {

    /**
     * todo 待优化
     * Processes metadata before registering the service.
     *
     * @param url      the service URL
     * @param metaData the associated metadata
     */
    void process(URL url, Map<String, String> metaData);
}


