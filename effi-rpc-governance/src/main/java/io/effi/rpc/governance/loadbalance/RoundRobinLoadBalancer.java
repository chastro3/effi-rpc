package io.effi.rpc.governance.loadbalance;

import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.spi.Extension;
import io.effi.rpc.config.URL;
import io.effi.rpc.util.AtomicUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.context.InvocationContext;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.constant.Component.LoadBalance.ROUND_ROBIN;

/**
 * "RoundRobin" load balancing strategy:
 * Requests are assigned to each server in sequence,
 * Each request is assigned in the order of the server list, and starts again at the end of the list,
 * This strategy applies to cases where the server performance is equivalent.
 */
@Extension(ROUND_ROBIN)
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected URL doChoose(InvocationContext<Envelope.Request, Caller<?>> context, List<URL> urls) {
        AtomicInteger lastIndex = context.invoker().get(KeyConstant.LAST_CALL_INDEX);
        int current = AtomicUtil.updateAtomicInteger(lastIndex, old -> (old + 1) % urls.size());
        return urls.get(current);
    }
}
