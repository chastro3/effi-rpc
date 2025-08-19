package io.effi.rpc.governance.registry;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.config.ExtensionKeys;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.registry.ServiceInstance;

import java.util.Collection;
import java.util.List;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.config.ConfigValues.DEFAULT;

/**
 * Discovers available services from registry center(s).
 */
@Extensible(
        value = DEFAULT,
        key = ExtensionKeys.SERVICE_DISCOVERY,
        scope = APPLICATION
)
public interface ServiceDiscovery {

    /**
     * Discovers services based on the given invocation context and registry configurations.
     *
     * @param context         the context for service discovery
     * @param registryConfigs the registry configurations
     * @return a list of URLs representing discovered services
     */
    List<ServiceInstance> discover(String serviceName, CallContext<Request, Caller<?>> context, Collection<RegistryConfig> registryConfigs);
}


