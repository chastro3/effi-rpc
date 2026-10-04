package io.effi.rpc.governance.lb;

import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Request;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.List;

/**
 * Provides an abstract implementation of {@link LoadBalancer}.
 */
public abstract class AbstractLoadBalancer implements LoadBalancer {

    private static final int MAX_WEIGHT = 10_000;

    @Override
    public ServiceInstance select(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances) {
        if (CollectionUtil.isEmpty(instances)) {
            throw InteractionErrorCodes.SERVICE_INSTANCE_NOT_FOUND.fail(context.message().url());
        }
        if (instances.size() == 1) {
            return instances.getFirst();
        }
        return doSelect(context, instances);
    }

    protected abstract ServiceInstance doSelect(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances);

    /**
     * Returns the positive selection weight declared by the instance metadata.
     *
     * @param instance the candidate service instance
     * @return the instance weight, defaulting to 1 when absent, invalid, or out of range
     */
    protected int weight(ServiceInstance instance) {
        String value = instance.metadata().get(KeyConstant.WEIGHT);
        if (StringUtil.isBlank(value)) {
            return 1;
        }
        try {
            int weight = Integer.parseInt(value.trim());
            return weight > 0 ? Math.min(weight, MAX_WEIGHT) : 1;
        } catch (NumberFormatException e) {
            return 1;
        }
    }

}
