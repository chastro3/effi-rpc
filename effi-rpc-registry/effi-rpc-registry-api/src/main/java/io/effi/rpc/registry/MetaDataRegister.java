package io.effi.rpc.registry;

import io.effi.rpc.common.spi.Extensible;
import io.effi.rpc.common.config.URL;

import java.util.Map;

/**
 * Manages metadata registration for services.
 * <p>Supports extending registered metadata during service registration.</p>
 */
@Extensible(lazyLoad = false)
public interface MetaDataRegister {

    /**
     * Processes metadata before service registration.
     *
     * @param url      the service URL
     * @param metaData the associated metadata
     */
    void process(URL url, Map<String, String> metaData);
}


