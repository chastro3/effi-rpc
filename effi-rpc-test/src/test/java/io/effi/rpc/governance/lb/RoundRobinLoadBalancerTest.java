package io.effi.rpc.governance.lb;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.registry.DefaultServiceInstance;
import io.effi.rpc.registry.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertSame;

class RoundRobinLoadBalancerTest {

    @Test
    void wrapsCounterWithoutNegativeIndex() {
        CallContext<Request, Caller<?>> context = context(new AtomicInteger(Integer.MAX_VALUE));
        List<ServiceInstance> instances = List.of(instance("a"), instance("b"), instance("c"));

        ServiceInstance selected = new RoundRobinLoadBalancer().select(context, instances);

        assertSame(instances.get(0), selected);
    }

    @Test
    void cyclesThroughCandidates() {
        CallContext<Request, Caller<?>> context = context(new AtomicInteger(-1));
        List<ServiceInstance> instances = List.of(instance("a"), instance("b"), instance("c"));
        RoundRobinLoadBalancer loadBalancer = new RoundRobinLoadBalancer();

        assertSame(instances.get(0), loadBalancer.select(context, instances));
        assertSame(instances.get(1), loadBalancer.select(context, instances));
        assertSame(instances.get(2), loadBalancer.select(context, instances));
        assertSame(instances.get(0), loadBalancer.select(context, instances));
    }

    @SuppressWarnings("unchecked")
    private static CallContext<Request, Caller<?>> context(AtomicInteger lastIndex) {
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
        return new CallContext<>(null, null, (Caller) caller, null, new Object[0]);
    }

    private static ServiceInstance instance(String id) {
        return DefaultServiceInstance.builder()
                .id(id)
                .serviceName("test-service")
                .protocol("http")
                .host("127.0.0.1")
                .port(8080)
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
}
