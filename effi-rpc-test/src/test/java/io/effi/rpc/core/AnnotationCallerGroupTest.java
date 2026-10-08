package io.effi.rpc.core;

import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.RpcType;
import io.effi.rpc.context.invocation.MethodInvocation;
import io.effi.rpc.context.invocation.PositionalInvocation;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.context.parameter.MethodBinder;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationCallerGroupTest {

    @Test
    void asyncCallReturnsCompletableFuture() {
        Caller<Object> caller = caller();

        Object result = AbstractCallerGroup.invokeCaller(
                caller,
                RpcType.ASYNC,
                new PositionalInvocation(new Object[0])
        );

        assertTrue(result instanceof CompletableFuture);
        assertEquals("ok", ((CompletableFuture<?>) result).join());
    }

    @Test
    void argumentWithoutWrapperIsPassedThrough() throws NoSuchMethodException {
        Method method = SampleClient.class.getDeclaredMethod("call", String.class);
        MethodBinding binding = MethodBinding.positional(method);
        MethodInvocation invocation = new MethodBinder(binding).bind(new Object[]{"value"});

        assertSame("value", invocation.arguments().get(0));
    }

    @SuppressWarnings("unchecked")
    private static Caller<Object> caller() {
        return (Caller<Object>) Proxy.newProxyInstance(
                AnnotationCallerGroupTest.class.getClassLoader(),
                new Class<?>[]{Caller.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "call" -> Promise.completed("ok");
                    case "blockingCall" -> "ok";
                    case "toString" -> "test-caller";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> throw new UnsupportedOperationException(method.getName());
                }
        );
    }

    private interface SampleClient {

        void call(String value);
    }
}
