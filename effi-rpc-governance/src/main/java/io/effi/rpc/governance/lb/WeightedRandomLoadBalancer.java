package io.effi.rpc.governance.lb;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.registry.ServiceInstance;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.effi.rpc.governance.lb.WeightedRandomLoadBalancer.NAME;

/**
 * Implements a weighted random load balancing strategy.
 * <p>
 * Instances with a larger {@code weight} metadata value are selected more often.
 */
@Extension(NAME)
public class WeightedRandomLoadBalancer extends AbstractLoadBalancer {

    public static final String NAME = "weightedRandom";

    @Override
    protected ServiceInstance doSelect(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances) {
        int total = 0;
        for (ServiceInstance instance : instances) {
            total += weight(instance);
        }
        int offset = ThreadLocalRandom.current().nextInt(total);
        for (ServiceInstance instance : instances) {
            offset -= weight(instance);
            if (offset < 0) {
                return instance;
            }
        }
        return instances.getLast();
    }
}
