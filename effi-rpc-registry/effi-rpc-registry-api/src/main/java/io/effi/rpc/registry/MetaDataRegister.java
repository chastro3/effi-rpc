package io.effi.rpc.registry;

import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.annotation.spi.Extensible;

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
    void process(ServiceHost serviceHost, Map<String, String> metaData);
}


