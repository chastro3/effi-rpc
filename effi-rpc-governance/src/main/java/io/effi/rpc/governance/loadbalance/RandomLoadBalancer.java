package io.effi.rpc.governance.loadbalance;

import io.effi.rpc.spi.Extension;
import io.effi.rpc.config.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.context.InvocationContext;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.effi.rpc.constant.Component.LoadBalance.RANDOM;

/**
 * Implements a "random" load balancing strategy.
 * <p>
 * Distributes requests to random servers in the cluster.
 * Fast and simple, but may cause uneven load distribution.
 * </p>
 */
@Extension(RANDOM)
public class RandomLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected URL doChoose(InvocationContext<Envelope.Request, Caller<?>> context, List<URL> urls) {
        int index = ThreadLocalRandom.current().nextInt(urls.size());
        return urls.get(index);
    }
}
