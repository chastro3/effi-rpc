package io.effi.rpc.governance.registry;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.metrics.DefaultMetrics;
import io.effi.rpc.component.registry.DefaultRegistryConfig;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.governance.GovernanceErrorCodes;
import io.effi.rpc.governance.metrics.GovernanceMetrics;
import io.effi.rpc.governance.router.Router;
import io.effi.rpc.metrics.CounterSample;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.metrics.Metrics;
import io.effi.rpc.metrics.MetricsSnapshot;
import io.effi.rpc.registry.DefaultServiceInstance;
import io.effi.rpc.registry.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistryLocatorRouteTest {

    @Test
    void failsWhenRoutingRemovesAllCandidates() {
        ScopedPlatform platform = new ScopedPlatform("route-empty-platform");
        try {
            ScopedApplication application = platform.newApplication("route-empty-application");
            ScopedModule module = application.newModule("route-empty-module");
            DefaultMetrics metrics = new DefaultMetrics(platform);
            GovernanceMetrics governanceMetrics = new GovernanceMetrics();
            platform.registry().register(Metrics.class, metrics);
            platform.registry().register(GovernanceMetrics.class, governanceMetrics);
            metrics.register(governanceMetrics);
            ServiceInstance instance = instance();
            application.registry().register(ServiceDiscovery.class, Constant.DEFAULT_NAME,
                    (ServiceDiscovery) (serviceName, context, configs) -> List.of(instance));
            application.registry().register(Router.class, Constant.DEFAULT_NAME,
                    (Router) (context, instances) -> List.of());
            io.effi.rpc.core.RegistryLocator locator = io.effi.rpc.core.RegistryLocator.cached(platform, "test-service", consulConfig());

            EffiRpcException failure = assertThrows(EffiRpcException.class,
                    () -> locator.locate(context(module)));

            assertEquals(GovernanceErrorCodes.ROUTE_NOT_MATCHED, failure.errorCode());
            assertEquals(1L, counter(metrics.snapshot(), GovernanceMetrics.ROUTE_COUNT.withTag("result", "empty")));
        } finally {
            platform.close();
        }
    }

    private static RegistryConfig consulConfig() {
        return DefaultRegistryConfig.builder()
                .type("consul")
                .address("consul://127.0.0.1:8500")
                .build();
    }

    private static ServiceInstance instance() {
        return DefaultServiceInstance.builder()
                .id("test-instance")
                .serviceName("test-service")
                .protocol("http")
                .host("127.0.0.1")
                .port(8080)
                .build();
    }

    private static long counter(MetricsSnapshot snapshot, MetricKey key) {
        return snapshot.samples().stream()
                .filter(CounterSample.class::isInstance)
                .map(CounterSample.class::cast)
                .filter(sample -> sample.key().equals(key))
                .mapToLong(CounterSample::value)
                .findFirst()
                .orElse(0L);
    }

    @SuppressWarnings("unchecked")
    private static CallContext<Request, Caller<?>> context(ScopedModule module) {
        Caller<?> caller = (Caller<?>) Proxy.newProxyInstance(
                Caller.class.getClassLoader(),
                new Class<?>[]{Caller.class},
                (proxy, method, args) -> defaultValue(method.getReturnType())
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
        return new CallContext<>(module, request, (Caller) caller, null, new Object[0]);
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
