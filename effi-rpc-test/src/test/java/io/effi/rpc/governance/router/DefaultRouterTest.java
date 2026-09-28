package io.effi.rpc.governance.router;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.config.RouterConfig;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.option.OptionName;
import io.effi.rpc.registry.DefaultServiceInstance;
import io.effi.rpc.registry.ServiceInstance;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultRouterTest {

    @Test
    void filtersByGroupAndRouterRule() {
        ServiceInstance blue = instance("blue");
        ServiceInstance green = instance("green");
        RouterConfig config = new RouterConfig(".*").match(".*blue.*");
        CallContext<Request, Caller<?>> context = context("blue", config);

        List<ServiceInstance> result = new DefaultRouter().route(context, List.of(blue, green));

        assertEquals(List.of(blue), result);
    }

    private static ServiceInstance instance(String group) {
        return DefaultServiceInstance.builder()
                .id(group + "-instance")
                .serviceName("test-service")
                .protocol("http/1.1")
                .host("127.0.0.1")
                .port(8080)
                .addMetadata(Map.of(KeyConstant.GROUP, group))
                .build();
    }

    @SuppressWarnings("unchecked")
    private static CallContext<Request, Caller<?>> context(String group, RouterConfig routerConfig) {
        ScopedPlatform platform = new ScopedPlatform("router-test-platform");
        ScopedApplication application = platform.newApplication("router-test-application");
        ScopedModule module = application.newModule("router-test-module");
        module.registry().register(RouterConfig.class, routerConfig);
        Caller<?> caller = proxy(Caller.class, (proxy, method, args) -> {
            if ("option".equals(method.getName())) {
                OptionName<?> option = (OptionName<?>) args[0];
                if (option == GovernanceOptions.GROUP) {
                    return group;
                }
            }
            return defaultValue(method.getReturnType());
        });
        Request request = proxy(Request.class, (proxy, method, args) -> {
            if ("url".equals(method.getName())) {
                return SmartURL.valueOf("http://127.0.0.1:8080/green");
            }
            return defaultValue(method.getReturnType());
        });
        return new CallContext<>(module, request, (Caller) caller, null, new Object[0]);
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
}
