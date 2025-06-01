package io.effi.rpc.governance.loadbalance;

import io.effi.rpc.config.URL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.annotation.spi.Extension;
import io.effi.rpc.util.AtomicUtil;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.constant.Component.LoadBalance.ROUND_ROBIN;

/**
 * Implements a "round-robin" load balancing strategy.
 * <p>
 * Distributes requests to servers in a fixed sequence, cycling through the server list.
 * Suitable when all servers have equivalent performance.
 * </p>
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
