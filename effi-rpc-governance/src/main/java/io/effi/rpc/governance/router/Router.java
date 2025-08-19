package io.effi.rpc.governance.router;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.config.ExtensionKeys;
import io.effi.rpc.registry.ServiceInstance;

import java.util.List;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.config.ConfigValues.DEFAULT;

/**
 * Routes a list of URLs based on the given invocation context.
 */
@Extensible(
        value = DEFAULT,
        key = ExtensionKeys.ROUTER,
        scope = APPLICATION
)
public interface Router {

    /**
     * Routes the provided list of URLs according to the invocation context.
     *
     * @param context the context for making routing decisions
     * @param urls    the list of URLs to route
     * @return a list of routed URLs
     */
    List<ServiceInstance> route(CallContext<?, Caller<?>> context, List<ServiceInstance> urls);
}


