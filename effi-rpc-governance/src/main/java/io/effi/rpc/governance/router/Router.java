package io.effi.rpc.governance.router;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.registry.ServiceInstance;

import java.util.List;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;

/**
 * Routes candidate service instances based on the given invocation context.
 */
@Extensible(scope = APPLICATION)
public interface Router {

    /**
     * Filters the provided service instances according to the invocation context.
     *
     * @param context the context for making routing decisions
     * @param instances the candidate service instances
     * @return the filtered service instances
     */
    List<ServiceInstance> route(CallContext<?, Caller<?>> context, List<ServiceInstance> instances);
}


