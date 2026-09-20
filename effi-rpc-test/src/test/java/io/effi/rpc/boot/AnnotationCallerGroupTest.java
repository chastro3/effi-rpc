package io.effi.rpc.boot;

import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.RpcType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnnotationCallerGroupTest {

    @Test
    void asyncCallReturnsCompletableFuture() {
        Caller<Object> caller = caller();

        Object result = AnnotationCallerGroup.invokeCaller(caller, RpcType.ASYNC, new Object[0]);

        assertTrue(result instanceof CompletableFuture);
        assertEquals("ok", ((CompletableFuture<?>) result).join());
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
}
