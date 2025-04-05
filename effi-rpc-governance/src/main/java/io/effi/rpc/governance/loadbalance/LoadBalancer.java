package io.effi.rpc.governance.loadbalance;

import io.effi.rpc.common.spi.Extensible;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.context.InvocationContext;

import java.util.List;

import static io.effi.rpc.common.constant.Component.LoadBalance.RANDOM;

/**
 * Load balancing strategies for selecting a URL from a list of available URLs
 * based on the context of the invocation.
 */
@Extensible(RANDOM)
public interface LoadBalancer {

    /**
     * Selects a URL for a given invocation from a list of available URLs.
     * Different implementations may employ various strategies for selection.
     *
     * @param context the context for the selection process
     * @param urls    a list of available URLs to choose from
     * @return the selected URL
     */
    URL choose(InvocationContext<Envelope.Request, Caller<?>> context, List<URL> urls);
}



