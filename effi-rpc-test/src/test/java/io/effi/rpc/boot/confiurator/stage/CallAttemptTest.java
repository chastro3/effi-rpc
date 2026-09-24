package io.effi.rpc.boot.confiurator.stage;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.support.Scheduler;
import io.effi.rpc.concurrent.Future;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.CallFutureRegistry;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.support.Unary;
import io.effi.rpc.context.options.CallerOptions;
import io.effi.rpc.option.OptionName;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.codec.ClientExchangeContextCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.endpoint.Client;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallAttemptTest {

    @Test
    void timeoutWhileAcquiringClosesLateChannelWithoutSending() throws Exception {
        TestContext fixture = new TestContext(20);
        Promise<Channel> acquire = new Promise<>();

        CallAttempt attempt = fixture.newAttempt(acquire);
        attempt.start();
        assertTimedOut(fixture.future);

        assertTrue(acquire.await().failed());
        assertNull(fixture.registry.lookup(fixture.future.id()));
    }

    @Test
    void timeoutAfterSendClosesActiveChannel() throws Exception {
        TestContext fixture = new TestContext(20);
        Promise<Channel> acquire = new Promise<>();
        TestChannel channel = new TestChannel();

        CallAttempt attempt = fixture.newAttempt(acquire);
        attempt.start();
        acquire.success(channel.proxy);

        assertTimedOut(fixture.future);

        assertEquals(1, channel.sends.get());
        assertEquals(1, channel.closes.get());
        assertNull(fixture.registry.lookup(fixture.future.id()));
    }

    private static void assertTimedOut(ReplyFuture future) throws Exception {
        assertThrows(
                ExecutionException.class,
                () -> future.toCompletableFuture().get(1, TimeUnit.SECONDS)
        );
        assertTrue(future.await().failed());
    }

    private static final class TestContext {

        private static final AtomicInteger PLATFORM_IDS = new AtomicInteger();

        private final ReplyFuture future;

        private final CallContext<Request, Caller<?>> context;

        private final CallFutureRegistry registry;

        private TestContext(long timeoutMillis) {
            ScopedPlatform platform = new ScopedPlatform("call-attempt-" + PLATFORM_IDS.incrementAndGet());
            platform.registry().register(Scheduler.class, new Scheduler());
            this.registry = new CallFutureRegistry();
            platform.registry().register(CallFutureRegistry.class, registry);
            ScopedApplication application = platform.newApplication("application");
            ScopedModule module = application.newModule("module");

            SmartURL url = SmartURL.valueOf("http://127.0.0.1:8080/test");
            Request request = proxy(Request.class, (proxy, method, args) -> switch (method.getName()) {
                case "url" -> url;
                case "needReply" -> true;
                default -> defaultValue(method.getReturnType());
            });
            Caller<?> caller = proxy(Caller.class, (proxy, method, args) -> {
                if ("option".equals(method.getName()) && args[0] instanceof OptionName<?> option) {
                    if (option == CallerOptions.TIMEOUT) {
                        return Math.toIntExact(timeoutMillis);
                    }
                }
                return defaultValue(method.getReturnType());
            });
            this.context = new CallContext<>(module, request, caller, Unary.MODE, new Object[0]);
            this.future = Unary.MODE.newFuture(context);
        }

        private CallAttempt newAttempt(Future<? extends Channel> acquire) {
            Client client = proxy(Client.class, (proxy, method, args) -> {
                if ("fetchChannel".equals(method.getName())) {
                    return acquire;
                }
                return defaultValue(method.getReturnType());
            });
            ClientExchangeContextCodec codec = proxy(ClientExchangeContextCodec.class, (proxy, method, args) ->
                    defaultValue(method.getReturnType())
            );
            TransportProtocol protocol = proxy(TransportProtocol.class, (proxy, method, args) -> {
                if ("clientCodec".equals(method.getName())) {
                    return codec;
                }
                return defaultValue(method.getReturnType());
            });
            return new CallAttempt(future, client, protocol, context);
        }
    }

    private static final class TestChannel {

        private final AtomicInteger sends = new AtomicInteger();

        private final AtomicInteger closes = new AtomicInteger();

        private final Channel proxy = proxy(Channel.class, (proxy, method, args) -> switch (method.getName()) {
            case "send" -> {
                sends.incrementAndGet();
                yield new Promise<Void>();
            }
            case "close" -> {
                closes.incrementAndGet();
                yield null;
            }
            case "active" -> closes.get() == 0;
            case "localAddress", "remoteAddress" -> InetSocketAddress.createUnresolved("127.0.0.1", 8080);
            default -> defaultValue(method.getReturnType());
        });
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
