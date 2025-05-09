package io.effi.rpc.governance.discovery;

import io.effi.rpc.config.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.spi.Extensible;

import java.util.List;

import static io.effi.rpc.constant.Component.DEFAULT;

/**
 * Discovers available services from registry center(s).
 */
@Extensible(DEFAULT)
public interface ServiceDiscovery {

    /**
     * Discovers services based on the given invocation context and registry configurations.
     *
     * @param context         the context for service discovery
     * @param registryConfigs the registry configurations
     * @return a list of URLs representing discovered services
     */
    List<URL> discover(InvocationContext<Envelope.Request, Caller<?>> context, URL... registryConfigs);
}


