package io.effi.rpc.governance.lb;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.governance.GovernanceErrorCodes;
import io.effi.rpc.registry.DefaultServiceInstance;
import io.effi.rpc.registry.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeightedLoadBalancerTest {

    @Test
    void weightedRoundRobinHonorsWeightRatios() {
        CallContext<Request, Caller<?>> context = context();
        ServiceInstance low = instance("low", "1");
        ServiceInstance high = instance("high", "3");
        List<ServiceInstance> instances = List.of(low, high);
        WeightedRoundRobinLoadBalancer loadBalancer = new WeightedRoundRobinLoadBalancer();

        assertSame(low, loadBalancer.select(context, instances));
        assertSame(high, loadBalancer.select(context, instances));
        assertSame(high, loadBalancer.select(context, instances));
        assertSame(high, loadBalancer.select(context, instances));
        assertSame(low, loadBalancer.select(context, instances));
    }

    @SuppressWarnings("unchecked")
    private static CallContext<Request, Caller<?>> context() {
        AtomicInteger lastIndex = new AtomicInteger(-1);
        Caller<?> caller = (Caller<?>) Proxy.newProxyInstance(
                Caller.class.getClassLoader(),
                new Class<?>[]{Caller.class},
                (proxy, method, args) -> {
                    if ("get".equals(method.getName())) {
                        return lastIndex;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
        Request request = (Request) Proxy.newProxyInstance(
                Request.class.getClassLoader(),
                new Class<?>[]{Request.class},
                (proxy, method, args) -> {
                    if ("url".equals(method.getName())) {
                        return SmartURL.builder()
                                .scheme("http")
                                .host("test-service")
                                .port(8080)
                                .path("test")
                                .build();
                    }
                    return defaultValue(method.getReturnType());
                }
        );
        return new CallContext<>(
                null,
                request,
                (Caller) caller,
                null,
                new Object[0]
        );
    }

    private static ServiceInstance instance(String id, String weight) {
        return DefaultServiceInstance.builder()
                .id(id)
                .serviceName("test-service")
                .protocol("http")
                .host("127.0.0.1")
                .port(8080)
                .addMetadata(Map.of(KeyConstant.WEIGHT, weight))
                .build();
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        return 0D;
    }

    @Test
    void weightedRoundRobinTreatsInvalidWeightAsOne() {
        CallContext<Request, Caller<?>> context = context();
        ServiceInstance invalid = instance("invalid", "abc");
        ServiceInstance zero = instance("zero", "0");
        List<ServiceInstance> instances = List.of(invalid, zero);
        WeightedRoundRobinLoadBalancer loadBalancer = new WeightedRoundRobinLoadBalancer();

        assertSame(invalid, loadBalancer.select(context, instances));
        assertSame(zero, loadBalancer.select(context, instances));
        assertSame(invalid, loadBalancer.select(context, instances));
        assertSame(zero, loadBalancer.select(context, instances));
    }

    @Test
    void weightedRandomSamplesAccordingToWeights() {
        CallContext<Request, Caller<?>> context = context();
        ServiceInstance low = instance("low", "1");
        ServiceInstance high = instance("high", "9");
        List<ServiceInstance> instances = List.of(low, high);
        WeightedRandomLoadBalancer loadBalancer = new WeightedRandomLoadBalancer();
        int lowCount = 0;

        for (int i = 0; i < 1000; i++) {
            if (loadBalancer.select(context, instances) == low) {
                lowCount++;
            }
        }

        assertTrue(lowCount > 50 && lowCount < 200, "unexpected weighted distribution: " + lowCount);
    }

    @Test
    void consistentHashKeepsExplicitHashKeyOnSameInstance() {
        CallContext<Request, Caller<?>> context = context();
        context.set(KeyConstant.HASH_KEY, "user-1");
        List<ServiceInstance> instances = List.of(instance("a", "1"), instance("b", "1"), instance("c", "1"));
        ConsistentHashLoadBalancer loadBalancer = new ConsistentHashLoadBalancer();

        ServiceInstance selected = loadBalancer.select(context, instances);

        for (int i = 0; i < 10; i++) {
            assertSame(selected, loadBalancer.select(context, instances));
        }
    }

    @Test
    void consistentHashRejectsMissingHashKey() {
        CallContext<Request, Caller<?>> context = context();
        List<ServiceInstance> instances = List.of(instance("a", "1"), instance("b", "1"));

        EffiRpcException failure = assertThrows(EffiRpcException.class,
                () -> new ConsistentHashLoadBalancer().select(context, instances));

        assertEquals(GovernanceErrorCodes.HASH_KEY_REQUIRED, failure.errorCode());
    }
}
