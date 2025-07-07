package io.effi.rpc.registry;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.base.ServiceHost;

import java.util.Map;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;

/**
 * Registers and extends service metadata during registration.
 */
@Extensible(lazyLoad = false, scope = APPLICATION)
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


