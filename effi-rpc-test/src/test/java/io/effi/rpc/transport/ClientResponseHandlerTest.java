package io.effi.rpc.transport;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.tools.ThreadPool;
import io.effi.rpc.concurrent.ConcurrentErrorCodes;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.CallFutureRegistry;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.ReplyContext;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.core.call.Unary;
import io.effi.rpc.transport.codec.ClientExchangeContextCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientResponseHandlerTest {

    @Test
    void rejectedResponseDeliveryCompletesReplyFuture() throws Exception {
        ScopedPlatform platform = new ScopedPlatform("client-response-test-platform");
        platform.registry().register(CallFutureRegistry.class, new CallFutureRegistry());
        ScopedApplication application = platform.newApplication("client-response-test-application");
        ScopedModule module = application.newModule("client-response-test-module");

        ExecutorService rejectingExecutor = Executors.newSingleThreadExecutor();
        rejectingExecutor.shutdown();
        ThreadPool rejectingPool = new ThreadPool("rejecting", rejectingExecutor);
        Caller<?> caller = caller(rejectingPool);

        SmartURL url = SmartURL.valueOf("http://127.0.0.1:8080/hello");
        Request request = request(url);
        CallContext<Request, Caller<?>> context = new CallContext<>(
                module,
                request,
                caller,
                Unary.MODE,
                new Object[0]
        );
        ReplyFuture replyFuture = new ReplyFuture(context);

        Response response = proxy(Response.class, (proxy, method, args) -> defaultValue(method.getReturnType()));
        ClientExchangeContextCodec codec = proxy(ClientExchangeContextCodec.class, (proxy, method, args) -> {
            if ("decode".equals(method.getName())) {
                return new ReplyContext<>(
                        context,
                        response,
                        Interaction.Result.success(url, "ok")
                );
            }
            return defaultValue(method.getReturnType());
        });
        TransportProtocol protocol = proxy(TransportProtocol.class, (proxy, method, args) -> {
            if ("clientCodec".equals(method.getName())) {
                return codec;
            }
            return defaultValue(method.getReturnType());
        });
        Channel channel = proxy(Channel.class, (proxy, method, args) -> switch (method.getName()) {
            case "platform" -> platform;
            case "protocol" -> protocol;
            case "remoteAddress" -> InetSocketAddress.createUnresolved("127.0.0.1", 8080);
            default -> defaultValue(method.getReturnType());
        });
        InputMessage inputMessage = proxy(InputMessage.class, (proxy, method, args) -> switch (method.getName()) {
            case "url" -> url;
            case "channel" -> channel;
            default -> defaultValue(method.getReturnType());
        });

        new ClientResponseHandler().handle(inputMessage);
        Result<ReplyContext<Response, Caller<?>>> result = replyFuture.await();

        assertTrue(result.failed());
        assertEquals(ConcurrentErrorCodes.TASK_REJECTED.code(), result.cause().errorCode().code());
    }

    private static Caller<?> caller(ThreadPool threadPool) {
        return proxy(Caller.class, (proxy, method, args) -> switch (method.getName()) {
            case "threadPool" -> threadPool;
            default -> defaultValue(method.getReturnType());
        });
    }

    private static Request request(SmartURL url) {
        return proxy(Request.class, (proxy, method, args) -> {
            if ("url".equals(method.getName())) {
                return url;
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
}
