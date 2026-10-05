package io.effi.rpc.governance.lb;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.AtomicUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.governance.lb.WeightedRoundRobinLoadBalancer.NAME;

/**
 * Implements a weighted round-robin load balancing strategy.
 * <p>
 * Selection follows the cumulative weight ranges of the candidate list.
 */
@Extension(NAME)
public class WeightedRoundRobinLoadBalancer extends AbstractLoadBalancer {

    public static final String NAME = "weightedRoundRobin";

    @Override
    protected ServiceInstance doSelect(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances) {
        int total = 0;
        for (ServiceInstance instance : instances) {
            total += weight(instance);
        }
        int range = total;
        AtomicInteger lastIndex = context.peer().get(KeyConstant.LAST_CALL_INDEX);
        int current = AtomicUtil.updateAtomicInteger(lastIndex, old -> old >= range - 1 ? 0 : old + 1);
        for (ServiceInstance instance : instances) {
            int weight = weight(instance);
            if (current < weight) {
                return instance;
            }
            current -= weight;
        }
        return instances.getLast();
    }
}
