package io.effi.rpc.governance.loadbalance;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.config.URL;
import io.effi.rpc.util.CollectionUtil;

import java.util.List;

import static io.effi.rpc.exception.PredefinedErrorCode.NOT_FOUND_SERVICE;

/**
 * Provides an abstract implementation of {@link LoadBalancer}.
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    @Override
    public URL select(CallContext<Message.Request, Caller<?>> context, List<URL> urls) {
        if (CollectionUtil.isEmpty(urls)) {
            throw NOT_FOUND_SERVICE.fail(null, context.message().url());
        }
        if (urls.size() == 1) {
            return urls.get(0);
        }
        return doChoose(context, urls);
    }

    protected abstract URL doChoose(CallContext<Message.Request, Caller<?>> context, List<URL> urls);

}
