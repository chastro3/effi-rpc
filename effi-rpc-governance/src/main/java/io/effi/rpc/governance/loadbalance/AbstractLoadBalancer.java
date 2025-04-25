package io.effi.rpc.governance.loadbalance;

import io.effi.rpc.config.URL;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.context.InvocationContext;

import java.util.List;

import static io.effi.rpc.exception.PredefinedErrorCode.NOT_FOUND_SERVICE;

/**
 * Abstract implementation of {@link LoadBalancer}.
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    @Override
    public URL choose(InvocationContext<Envelope.Request, Caller<?>> context, List<URL> urls) {
        if (CollectionUtil.isEmpty(urls)) {
            throw NOT_FOUND_SERVICE.fail(null, context.source().url());
        }
        if (urls.size() == 1) {
            return urls.getFirst();
        }
        return doChoose(context, urls);
    }

    protected abstract URL doChoose(InvocationContext<Envelope.Request, Caller<?>> context, List<URL> urls);

}
