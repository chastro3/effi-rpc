package io.effi.rpc.governance.router;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.config.ExtensionKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.constant.Component;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.annotation.spi.Extensible;
import io.effi.rpc.util.GenericKey;

import java.util.List;

/**
 * Routes a list of URLs based on the given invocation context.
 */
@Extensible(value = Component.DEFAULT, key = ExtensionKeys.ROUTER)
public interface Router {

    GenericKey<Router> ATTRIBUTE_KEY = GenericKey.valueOf(KeyConstant.ROUTER);

    /**
     * Routes the provided list of URLs according to the invocation context.
     *
     * @param context the context for making routing decisions
     * @param urls    the list of URLs to route
     * @return a list of routed URLs
     */
    List<URL> route(InvocationContext<?, Caller<?>> context, List<URL> urls);
}


