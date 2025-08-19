package io.effi.rpc.governance.lb;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.registry.ServiceInstance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.effi.rpc.config.ConfigValues.LoadBalance.RANDOM;


/**
 * Implements a "random" load balancing strategy.
 * <p>
 * Distributes requests to random servers in the cluster.
 * Fast and simple, but may cause uneven load distribution.
 */
@Extension(RANDOM)
public class RandomLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected ServiceInstance doSelect(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances) {
        int index = ThreadLocalRandom.current().nextInt(instances.size());
        return instances.get(index);
    }
}
