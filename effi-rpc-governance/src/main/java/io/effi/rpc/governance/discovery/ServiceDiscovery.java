package io.effi.rpc.governance.discovery;

import io.effi.rpc.common.spi.Extensible;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.context.InvocationContext;

import java.util.List;

import static io.effi.rpc.common.constant.Component.DEFAULT;

/**
 * Discovers available services from a service registry.
 */
@Extensible(DEFAULT)
public interface ServiceDiscovery {

    /**
     * Retrieves all available services from the registry based on the provided
     * invocation context and registry configurations.
     *
     * @param context         the context for the service lookup
     * @param registryConfigs the configurations for accessing the service registry
     * @return a list of URLs representing the available services
     */
    List<URL> discover(InvocationContext<Envelope.Request, Caller<?>> context, URL... registryConfigs);
}


