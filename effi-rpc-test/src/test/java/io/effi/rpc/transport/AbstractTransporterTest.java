package io.effi.rpc.transport;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class AbstractTransporterTest {

    @Test
    void clientCacheIncludesClientConfigIdentity() {
        ScopedPlatform platform = new ScopedPlatform("transporter-cache-test-platform");
        CountingTransporter transporter = new CountingTransporter();
        InetSocketAddress address = InetSocketAddress.createUnresolved("127.0.0.1", 8080);
        ClientConfig firstConfig = config("client-a");
        ClientConfig sameIdentity = config("client-a");
        ClientConfig otherConfig = config("client-b");

        Client first = transporter.supplyClient(firstConfig, address, platform);
        Client same = transporter.supplyClient(sameIdentity, address, platform);
        Client other = transporter.supplyClient(otherConfig, address, platform);

        assertSame(first, same);
        assertNotSame(first, other);
        assertEquals(2, transporter.created.get());
        transporter.clear();
        platform.close();
    }

    private static ClientConfig config(String id) {
        return proxy(ClientConfig.class, (proxy, method, args) -> {
            if ("id".equals(method.getName())) {
                return id;
            }
            return defaultValue(method.getReturnType());
        });
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
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

    private static final class CountingTransporter extends AbstractTransporter {

        private final AtomicInteger created = new AtomicInteger();

        @Override
        protected Server createServer(ServerConfig config, InetSocketAddress address, ScopedPlatform platform) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Client createClient(ClientConfig config, InetSocketAddress remoteAddress, ScopedPlatform platform) {
            created.incrementAndGet();
            return proxy(Client.class, (proxy, method, args) -> defaultValue(method.getReturnType()));
        }
    }
}
