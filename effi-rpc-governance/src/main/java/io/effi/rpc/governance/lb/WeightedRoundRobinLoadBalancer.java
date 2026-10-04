package io.effi.rpc.governance.lb;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.registry.ServiceInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.effi.rpc.governance.lb.WeightedRoundRobinLoadBalancer.NAME;

/**
 * Implements a smooth weighted round-robin load balancing strategy.
 * <p>
 * Selection spreads weighted traffic across candidates instead of emitting each weight as a
 * consecutive burst.
 */
@Extension(NAME)
public class WeightedRoundRobinLoadBalancer extends AbstractLoadBalancer {

    public static final String NAME = "weightedRoundRobin";

    private final Map<String, InstanceWeight> instanceWeights = new HashMap<>();

    @Override
    protected synchronized ServiceInstance doSelect(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances) {
        int total = 0;
        InstanceWeight selected = null;
        ServiceInstance selectedInstance = null;
        for (ServiceInstance instance : instances) {
            int weight = weight(instance);
            total += weight;
            InstanceWeight current = instanceWeights.computeIfAbsent(instance.id(), id -> new InstanceWeight());
            current.value += weight;
            if (selected == null || current.value > selected.value) {
                selected = current;
                selectedInstance = instance;
            }
        }
        selected.value -= total;
        return selectedInstance;
    }

    private static final class InstanceWeight {

        private int value;
    }
}
