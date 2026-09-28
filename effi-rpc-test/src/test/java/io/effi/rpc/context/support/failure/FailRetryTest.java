package io.effi.rpc.context.support.failure;

import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.options.FaultToleranceOptions;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.option.OptionName;
import io.effi.rpc.util.GenericKey;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FailRetryTest {

    @Test
    void doesNotRetryUnmarkedFailure() {
        EffiRpcException failure = PredefinedErrorCode.COMMON.fail("business failure");

        EffiRpcException thrown = assertThrows(
                EffiRpcException.class,
                () -> new FailRetry().handle(context(new CallerMetrics()), 1, failure)
        );

        assertSame(failure, thrown);
    }

    @Test
    void retriesMarkedTransientFailure() throws EffiRpcException {
        CallerMetrics metrics = new CallerMetrics();
        EffiRpcException failure = PredefinedErrorCode.SERVICE_UNAVAILABLE
                .fail("overloaded")
                .withMetadata(Map.of(KeyConstant.RETRYABLE, Boolean.TRUE.toString()));

        new FailRetry().handle(context(metrics), 1, failure);

        assertEquals(1L, metrics.retryCount().sum());
    }

    private static CallContext<Request, Caller<?>> context(CallerMetrics metrics) {
        Caller<?> caller = proxy(Caller.class, (proxy, method, args) -> {
            if ("option".equals(method.getName())) {
                OptionName<?> option = (OptionName<?>) args[0];
                if (option == FaultToleranceOptions.RETRIES) {
                    return 2;
                }
            }
            if ("get".equals(method.getName()) && args[0] == CallerMetrics.GENERIC_KEY) {
                return metrics;
            }
            return defaultValue(method.getReturnType());
        });
        Request request = proxy(Request.class, (proxy, method, args) -> {
            if ("url".equals(method.getName())) {
                return SmartURL.valueOf("http://127.0.0.1:8080/test");
            }
            return defaultValue(method.getReturnType());
        });
        return new CallContext<>(null, request, (Caller) caller, null, new Object[0]);
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
