package io.effi.rpc.governance.interceptor;

import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.options.GovernanceOptions;
import io.effi.rpc.option.OptionName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class HashKeyInterceptorTest {

    @Test
    void resolvesHashKeyFromDeclaredArgumentIndex() {
        CallContext<Request, Caller<?>> context = context(0, new Object[]{"tenant-1"});

        new HashKeyInterceptor().intercept(context, chain());

        assertEquals("tenant-1", context.get(KeyConstant.HASH_KEY));
    }

    @Test
    void leavesHashKeyUnsetWhenIndexIsMissing() {
        CallContext<Request, Caller<?>> context = context(-1, new Object[]{"tenant-1"});

        new HashKeyInterceptor().intercept(context, chain());

        assertNull(context.get(KeyConstant.HASH_KEY));
    }

    @SuppressWarnings("unchecked")
    private static CallContext<Request, Caller<?>> context(int index, Object[] args) {
        Caller<?> caller = (Caller<?>) Proxy.newProxyInstance(
                Caller.class.getClassLoader(),
                new Class<?>[]{Caller.class},
                (proxy, method, methodArgs) -> {
                    if ("option".equals(method.getName())) {
                        OptionName<?> option = (OptionName<?>) methodArgs[0];
                        return option == GovernanceOptions.HASH_KEY_INDEX ? index : null;
                    }
                    return defaultValue(method.getReturnType());
                }
        );
        return new CallContext<>(
                null,
                null,
                (Caller) caller,
                null,
                args
        );
    }

    private static Interceptor.Chain chain() {
        return new Interceptor.Chain() {
            @Override
            @SuppressWarnings("rawtypes")
            public <C extends Interaction.Context> Interaction.Result proceed(C context) {
                return null;
            }
        };
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
