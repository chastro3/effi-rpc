package io.effi.rpc.governance.lb;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.registry.ServiceInstance;

import java.util.List;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;

/**
 * Selects a target service instance from available candidates using a load balancing strategy.
 */
@Extensible(scope = APPLICATION)
public interface LoadBalancer {

    /**
     * Selects a service instance from the given list based on the invocation context.
     *
     * @param context the invocation context
     * @param urls    the list of available service instance(s)
     * @return the selected service instance
     */
    ServiceInstance select(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances);
}



