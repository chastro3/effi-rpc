package io.effi.rpc.governance.loadbalance;

import io.effi.rpc.spi.Extensible;
import io.effi.rpc.config.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.context.InvocationContext;

import java.util.List;

import static io.effi.rpc.constant.Component.LoadBalance.RANDOM;

/**
 * Selects a target URL from available candidates using a load balancing strategy.
 */
@Extensible(RANDOM)
public interface LoadBalancer {

    /**
     * Selects a URL from the given list based on the invocation context.
     *
     * @param context the invocation context
     * @param urls    the list of available URLs
     * @return the selected URL
     */
    URL select(InvocationContext<Envelope.Request, Caller<?>> context, List<URL> urls);
}



