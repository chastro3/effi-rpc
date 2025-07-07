package io.effi.rpc.governance.loadbalance;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.config.URL;

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
    protected URL doChoose(CallContext<Message.Request, Caller<?>> context, List<URL> urls) {
        int index = ThreadLocalRandom.current().nextInt(urls.size());
        return urls.get(index);
    }
}
