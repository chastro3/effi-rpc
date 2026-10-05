package io.effi.rpc.governance.lb;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.governance.GovernanceErrorCodes;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.StringUtil;

import java.util.List;

import static io.effi.rpc.governance.lb.ConsistentHashLoadBalancer.NAME;

/**
 * Implements a rendezvous consistent-hash load balancing strategy.
 * <p>
 * The hash key is read from {@link KeyConstant#HASH_KEY}. Instances are associated by their
 * protocol, host, and port so the mapping does not depend on registry-assigned instance ids.
 */
@Extension(NAME)
public class ConsistentHashLoadBalancer extends AbstractLoadBalancer {

    public static final String NAME = "consistentHash";

    @Override
    protected ServiceInstance doSelect(CallContext<Request, Caller<?>> context, List<ServiceInstance> instances) {
        String key = context.get(KeyConstant.HASH_KEY);
        if (StringUtil.isBlank(key)) {
            throw GovernanceErrorCodes.HASH_KEY_REQUIRED.fail();
        }
        ServiceInstance selected = instances.getFirst();
        long selectedScore = Long.MIN_VALUE;
        for (ServiceInstance instance : instances) {
            long score = score(key, instanceKey(instance));
            if (score > selectedScore) {
                selected = instance;
                selectedScore = score;
            }
        }
        return selected;
    }

    private String instanceKey(ServiceInstance instance) {
        return instance.protocol() + "://" + instance.host() + ":" + instance.port();
    }

    private long score(String key, String instanceKey) {
        // Finalize the mixed hash so adjacent keys spread across the full score range.
        long hash = key.hashCode();
        hash = 31 * hash + instanceKey.hashCode();
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= hash >>> 33;
        return hash;
    }
}
