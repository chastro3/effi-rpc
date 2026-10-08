package io.effi.rpc.governance.router;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.registry.DefaultServiceInstance;
import io.effi.rpc.registry.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultRouterTest {

    @Test
    void appliesFirstUrlMatchingRule() {
        ServiceInstance blue = instance("blue-instance", Map.of("zone", "blue"));
        ServiceInstance green = instance("green-instance", Map.of("zone", "green"));
        RouterConfig config = RouterConfig.builder()
                .rule(".*green", Map.of("zone", "blue"))
                .rule(".*green", Map.of("zone", "green"))
                .build();
        CallContext<Request, Caller<?>> context = context(config);

        List<ServiceInstance> result = new DefaultRouter().route(context, List.of(blue, green));

        assertEquals(List.of(blue), result);
    }

    private static ServiceInstance instance(String id, Map<String, String> metadata) {
        return DefaultServiceInstance.builder()
                .id(id)
                .serviceName("test-service")
                .protocol("http/1.1")
                .host("127.0.0.1")
                .port(8080)
                .addMetadata(metadata)
                .build();
    }

    @SuppressWarnings("unchecked")
    private static CallContext<Request, Caller<?>> context(RouterConfig routerConfig) {
        ScopedPlatform platform = new ScopedPlatform("router-test-platform");
        ScopedApplication application = platform.newApplication("router-test-application");
        ScopedModule module = application.newModule("router-test-module");
        module.registry().register(RouterConfig.class, routerConfig);
        Caller<?> caller = proxy(Caller.class, (proxy, method, args) -> defaultValue(method.getReturnType()));
        Request request = proxy(Request.class, (proxy, method, args) -> {
            if ("url".equals(method.getName())) {
                return SmartURL.valueOf("http://127.0.0.1:8080/green");
            }
            return defaultValue(method.getReturnType());
        });
        return new CallContext<>(
                module,
                request,
                (Caller) caller,
                null,
                new Object[0]
        );
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
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
    void passesThroughWhenNoUrlRuleMatches() {
        ServiceInstance blue = instance("blue-instance", Map.of("zone", "blue"));
        ServiceInstance green = instance("green-instance", Map.of("zone", "green"));
        RouterConfig config = RouterConfig.builder()
                .rule(".*canary", Map.of("zone", "blue"))
                .build();
        CallContext<Request, Caller<?>> context = context(config);

        List<ServiceInstance> result = new DefaultRouter().route(context, List.of(blue, green));

        assertEquals(List.of(blue, green), result);
    }

    @Test
    void returnsEmptyWhenMetadataMatchesNoInstance() {
        ServiceInstance blue = instance("blue-instance", Map.of("zone", "blue"));
        RouterConfig config = RouterConfig.builder()
                .rule(".*green", Map.of("zone", "red"))
                .build();
        CallContext<Request, Caller<?>> context = context(config);

        List<ServiceInstance> result = new DefaultRouter().route(context, List.of(blue));

        assertEquals(List.of(), result);
    }
}
