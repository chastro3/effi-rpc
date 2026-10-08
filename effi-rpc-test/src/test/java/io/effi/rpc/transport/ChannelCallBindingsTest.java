package io.effi.rpc.transport;

import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.context.CallFutureRegistry;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.transport.endpoint.Channel;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChannelCallBindingsTest {

    @Test
    void cancelChannelCancelsBoundCalls() throws Exception {
        CallFutureRegistry futures = new CallFutureRegistry();
        ChannelCallBindings bindings = new ChannelCallBindings(futures);
        Promise<String> future = new Promise<>();
        long callId = futures.register(future);
        Channel channel = channel();
        assertTrue(bindings.bind(callId, channel));

        EffiRpcException reason = TransportErrorCodes.CHANNEL_INACTIVE.fail(channel);
        bindings.cancelChannel(channel, reason);

        assertSame(reason, future.await().cause());
        assertNull(futures.lookup(callId));
        assertEquals(0, bindings.size());
    }

    private static Channel channel() {
        return (Channel) Proxy.newProxyInstance(
                Channel.class.getClassLoader(),
                new Class<?>[]{Channel.class},
                (proxy, method, args) -> {
                    if ("active".equals(method.getName())) {
                        return true;
                    }
                    if ("localAddress".equals(method.getName()) || "remoteAddress".equals(method.getName())) {
                        return InetSocketAddress.createUnresolved("127.0.0.1", 8080);
                    }
                    return defaultValue(method.getReturnType());
                }
        );
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
    void terminalFutureAutomaticallyUnbindsChannel() {
        CallFutureRegistry futures = new CallFutureRegistry();
        ChannelCallBindings bindings = new ChannelCallBindings(futures);
        Promise<String> future = new Promise<>();
        long callId = futures.register(future);
        Channel channel = channel();
        bindings.bind(callId, channel);

        future.success("ok");

        assertEquals(0, bindings.size());
    }

    @Test
    void closeCancelsBoundCalls() throws Exception {
        CallFutureRegistry futures = new CallFutureRegistry();
        ChannelCallBindings bindings = new ChannelCallBindings(futures);
        Promise<String> future = new Promise<>();
        long callId = futures.register(future);
        bindings.bind(callId, channel());

        bindings.close();

        assertEquals(TransportErrorCodes.CALL_BINDINGS_CLOSED.code(), future.await().cause().errorCode().code());
        assertNull(futures.lookup(callId));
        assertEquals(0, bindings.size());
    }
}
