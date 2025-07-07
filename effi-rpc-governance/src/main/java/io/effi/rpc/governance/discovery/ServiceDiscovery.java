package io.effi.rpc.governance.discovery;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.config.ExtensionKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.registry.RegistryConfig;

import java.util.List;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.constant.Component.DEFAULT;

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
    List<URL> discover(String serviceName, CallContext<Message.Request, Caller<?>> context, List<RegistryConfig> registryConfigs);
}


