package io.effi.rpc.governance.router;

import io.effi.rpc.constant.Component;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.spi.Extensible;
import io.effi.rpc.config.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.context.InvocationContext;

import java.util.List;

/**
 * Routes a list of URLs based on the provided invocation context.
 */
@Extensible(Component.DEFAULT)
public interface Router {

    GenericKey<Router> ATTRIBUTE_KEY = GenericKey.valueOf(KeyConstant.ROUTER);

    /**
     * Routes the list of URLs according to the given invocation context.
     *
     * @param context the context for routing decisions
     * @param urls    the list of URLs to be routed
     * @return a list of routed URLs
     */
    List<URL> route(InvocationContext<?, Caller<?>> context, List<URL> urls);
}


