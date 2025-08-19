package io.effi.rpc.governance.lb;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.config.ExtensionKeys;
import io.effi.rpc.registry.ServiceInstance;

import java.util.List;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.config.ConfigValues.LoadBalance.RANDOM;

/**
 * Selects a target URL from available candidates using a load balancing strategy.
 */
@Extensible(
        value = RANDOM,
        key = ExtensionKeys.LOAD_BALANCER,
        scope = APPLICATION
)
public interface LoadBalancer {

    /**
     * Selects a URL from the given list based on the invocation context.
     *
     * @param context the invocation context
     * @param urls    the list of available URLs
     * @return the selected URL
     */
    ServiceInstance select(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances);
}



