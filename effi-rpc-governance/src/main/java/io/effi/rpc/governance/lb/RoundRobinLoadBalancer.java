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

import static io.effi.rpc.governance.lb.RoundRobinLoadBalancer.NAME;


/**
 * Implements a "round-robin" load balancing strategy.
 * <p>
 * Distributes requests to servers in a fixed sequence, cycling through the server list.
 * Suitable when all servers have equivalent performance.
 */
@Extension(NAME)
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    public static final String NAME = "roundRobin";

    @Override
    protected ServiceInstance doSelect(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances) {
        AtomicInteger lastIndex = context.peer().get(KeyConstant.LAST_CALL_INDEX);
        int current = AtomicUtil.updateAtomicInteger(lastIndex, old -> (old + 1) % instances.size());
        return instances.get(current);
    }
}
