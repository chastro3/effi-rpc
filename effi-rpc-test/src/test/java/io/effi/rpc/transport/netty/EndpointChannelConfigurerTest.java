package io.effi.rpc.transport.netty;

import io.effi.rpc.transport.endpoint.Client;
import io.netty.buffer.ByteBufAllocator;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EndpointChannelConfigurerTest {

    @Test
    void clientSslHandlerEnablesSniAndHostnameVerification() throws Exception {
        Client client = proxy(Client.class, (proxy, method, args) -> {
            if ("remoteAddress".equals(method.getName())) {
                return InetSocketAddress.createUnresolved("example.com", 8443);
            }
            return defaultValue(method.getReturnType());
        });
        EndpointChannelConfigurer<Client> configurer = new EndpointChannelConfigurer<>(
                client,
                SslContextBuilder.forClient().build()
        ) {
        };

        SslHandler handler = configurer.createSslHandler(ByteBufAllocator.DEFAULT);

        assertEquals("example.com", handler.engine().getPeerHost());
        assertEquals(8443, handler.engine().getPeerPort());
        assertEquals("HTTPS", handler.engine().getSSLParameters().getEndpointIdentificationAlgorithm());
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
