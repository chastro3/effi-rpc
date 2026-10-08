package io.effi.rpc.context;

import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.invocation.MethodInvocation;
import io.effi.rpc.context.parameter.MethodBinder;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.context.parameter.ParameterBinder;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.util.GenericKey;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MethodBindingTest {

    @Test
    void positionalBindingKeepsMethodSignatureAndArgumentsInArrays() throws Exception {
        Method method = Sample.class.getMethod("hello", String.class, int.class);
        MethodBinding binding = MethodBinding.positional(method);

        assertArrayEquals(method.getParameterTypes(), binding.signature().parameterTypes());
        assertEquals(2, binding.parameters().length);

        MethodInvocation invocation = new MethodBinder(binding).bind(new Object[]{"tom", 18});

        assertArrayEquals(method.getParameterTypes(), invocation.signature().parameterTypes());
        assertEquals(2, invocation.arguments().size());
        assertEquals("tom", invocation.arguments().get(0));
        assertEquals(18, invocation.arguments().get(1));
        assertEquals("hello", invocation.signature().name());
    }

    @Test
    void namedBindingDoesNotCreatePositionalArguments() throws Exception {
        Method method = Sample.class.getMethod("named", String.class);
        GenericKey<Object> key = GenericKey.valueOf("test.named");
        ParameterBinder namedBinder = new ParameterBinder() {
            @Override
            public void write(Object value, ParameterBinding binding, Invocation invocation) {
                invocation.set(key, value);
            }

            @Override
            public Object resolve(ParameterBinding binding, Request request, Peer peer) {
                return null;
            }
        };
        MethodBinding binding = MethodBinding.of(method, new ParameterBinding[]{
                new ParameterBinding(0, method.getParameters()[0], namedBinder)
        });
        MethodBinder binder = new MethodBinder(binding);

        MethodInvocation invocation = binder.bind(new Object[]{"tom"});

        assertTrue(invocation.arguments().isEmpty());
        assertEquals("tom", invocation.get(key));
    }

    private interface Sample {

        String hello(String name, int age);

        String named(String name);
    }
}
