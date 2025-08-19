package io.effi.rpc.governance.lb;

import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.CollectionUtil;

import java.util.List;

import static io.effi.rpc.exception.PredefinedErrorCode.NOT_FOUND_SERVICE;

/**
 * Provides an abstract implementation of {@link LoadBalancer}.
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    @Override
    public ServiceInstance select(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances) {
        if (CollectionUtil.isEmpty(instances)) {
            throw NOT_FOUND_SERVICE.fail(null, context.message().url());
        }
        if (instances.size() == 1) {
            return instances.get(0);
        }
        return doSelect(context, instances);
    }

    protected abstract ServiceInstance doSelect(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances);

}
