package io.effi.rpc.governance.loadbalance;

import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.context.InvocationContext;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static io.effi.rpc.common.constant.Component.LoadBalance.RANDOM;

/**
 * "Random" load balancing strategy:
 * Randomly assign requests to any server in the server cluster,
 * This strategy is simple and fast, but it can lead to an uneven server load.
 */
@Extension(RANDOM)
public class RandomLoadBalancer extends AbstractLoadBalancer {

    @Override
    protected URL doChoose(InvocationContext<?, Caller<?>> context, List<URL> urls) {
        int index = ThreadLocalRandom.current().nextInt(urls.size());
        return urls.get(index);
    }
}
