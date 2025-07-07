package io.effi.rpc.governance.router;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.config.ExtensionKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.constant.Component;

import java.util.List;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;

/**
 * Routes a list of URLs based on the given invocation context.
 */
@Extensible(
        value = Component.DEFAULT,
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
    List<URL> route(CallContext<?, Caller<?>> context, List<URL> urls);
}


