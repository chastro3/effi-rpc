package io.effi.rpc.proxy;

import io.effi.rpc.proxy.bytebuddy.ByteBuddyProxyFactory;
import io.effi.rpc.proxy.cglib.CGLibProxyFactory;
import io.effi.rpc.proxy.jdk.JDKProxyFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProxyFactoryTest {

    static Stream<ProxyFactory> interfaceFactories() {
        return Stream.of(new JDKProxyFactory(), new CGLibProxyFactory(), new ByteBuddyProxyFactory());
    }

    static Stream<ProxyFactory> targetFactories() {
        return Stream.of(new JDKProxyFactory(), new CGLibProxyFactory(), new ByteBuddyProxyFactory());
    }

    @ParameterizedTest
    @MethodSource("interfaceFactories")
    void interfaceProxyDispatchesHandler(ProxyFactory factory) {
        Greeter greeter = factory.createProxy(Greeter.class,
                (proxy, method, args, superInvoker) -> "rpc:" + args[0]);
        assertEquals("rpc:world", greeter.greet("world"));
    }

    @ParameterizedTest
    @MethodSource("interfaceFactories")
    void interfaceProxyInvokesDefaultMethodLocally(ProxyFactory factory) {
        Greeter greeter = factory.createProxy(Greeter.class, (proxy, method, args, superInvoker) -> {
            throw new AssertionError("Handler must not receive default methods");
        });
        assertEquals("yo", greeter.casual());
    }

    @ParameterizedTest
    @MethodSource("interfaceFactories")
    void interfaceProxyUsesIdentityObjectMethods(ProxyFactory factory) {
        Greeter greeter = factory.createProxy(Greeter.class, (proxy, method, args, superInvoker) -> {
            throw new AssertionError("Handler must not receive Object methods");
        });
        assertEquals(System.identityHashCode(greeter), greeter.hashCode());
        assertTrue(greeter.equals(greeter));
        assertFalse(greeter.equals(null));
        assertTrue(greeter.toString().contains("@"));
    }

    @ParameterizedTest
    @MethodSource("targetFactories")
    void targetProxyDelegatesToTarget(ProxyFactory factory) {
        DefaultCounter target = new DefaultCounter();
        Counter counter = factory.createProxy(target, (proxy, method, args, superInvoker) -> superInvoker.invoke());
        assertEquals(7, counter.value());
        target.value(11);
        assertEquals(11, counter.value());
    }

    @ParameterizedTest
    @MethodSource("targetFactories")
    void targetProxyUnwrapsTargetException(ProxyFactory factory) {
        DefaultCounter target = new DefaultCounter();
        Counter counter = factory.createProxy(target, (proxy, method, args, superInvoker) -> superInvoker.invoke());
        IllegalStateException error = assertThrows(IllegalStateException.class, counter::fail);
        assertEquals("boom", error.getMessage());
    }

    @Test
    void interfaceCreationRejectsConcreteType() {
        assertThrows(IllegalArgumentException.class, () -> new JDKProxyFactory()
                .createProxy(String.class, (proxy, method, args, superInvoker) -> null));
    }

    interface Greeter {

        String greet(String name);

        default String casual() {
            return "yo";
        }
    }

    interface Counter {

        int value();

        void value(int value);

        String fail();
    }

    static class DefaultCounter implements Counter {

        private int value = 7;

        @Override
        public int value() {
            return value;
        }

        @Override
        public void value(int value) {
            this.value = value;
        }

        @Override
        public String fail() {
            throw new IllegalStateException("boom");
        }
    }
}
