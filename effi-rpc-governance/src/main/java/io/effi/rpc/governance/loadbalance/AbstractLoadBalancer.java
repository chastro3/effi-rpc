package io.effi.rpc.governance.loadbalance;

import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.context.InvocationContext;

import java.util.List;

/**
 * Abstract implementation of {@link LoadBalancer}.
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    @Override
    public URL choose(InvocationContext<?, Caller<?>> context, List<URL> urls) {
        if (CollectionUtil.isEmpty(urls)) {
            throw EffiRpcException.wrap(
                    PredefinedErrorCode.NOT_FOUND_SERVICE,
                    context.source().url()
            );
        }
        if (urls.size() == 1) {
            return urls.getFirst();
        }
        return doChoose(context, urls);
    }

    protected abstract URL doChoose(InvocationContext<?, Caller<?>> context, List<URL> urls);

}
