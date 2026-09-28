package io.effi.rpc.transport;

import io.effi.rpc.concurrent.Futures;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Response;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.transport.codec.ServerExchangeContextCodec;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerExchangeTest {

    @Test
    void oneWayRequestDoesNotCreateOrSendErrorResponse() {
        AtomicInteger creates = new AtomicInteger();
        ServerExchange exchange = exchange(false, creates, new AtomicInteger());

        exchange.fail(PredefinedErrorCode.COMMON.fail("failure"));

        assertEquals(0, creates.get());
    }

    @Test
    void errorResponseIsEncodedByServerCodecAndSentThroughChannel() {
        AtomicInteger sends = new AtomicInteger();
        ServerExchange exchange = exchange(true, new AtomicInteger(), sends);

        exchange.fail(PredefinedErrorCode.COMMON.fail("failure"));

        assertEquals(1, sends.get());
    }

    private static ServerExchange exchange(
            boolean needReply,
            AtomicInteger creates,
            AtomicInteger sends
    ) {
        ServerExchangeContextCodec codec = proxy(ServerExchangeContextCodec.class, (proxy, method, args) -> {
            if ("encode".equals(method.getName()) && args[0] instanceof Response) {
                return proxy(OutputMessage.class, (ignored, ignoredMethod, ignoredArgs) -> null);
            }
            return defaultValue(method.getReturnType());
        });
        TransportProtocol protocol = proxy(TransportProtocol.class, (proxy, method, args) -> {
            if ("createErrorResponse".equals(method.getName())) {
                creates.incrementAndGet();
                return proxy(Response.class, (ignored, ignoredMethod, ignoredArgs) -> null);
            }
            if ("serverCodec".equals(method.getName())) {
                return codec;
            }
            return defaultValue(method.getReturnType());
        });
        Channel channel = proxy(Channel.class, (proxy, method, args) -> switch (method.getName()) {
            case "protocol" -> protocol;
            case "send" -> {
                sends.incrementAndGet();
                yield Futures.completedVoid();
            }
            default -> defaultValue(method.getReturnType());
        });
        InputMessage inputMessage = (InputMessage) Proxy.newProxyInstance(
                InputMessage.class.getClassLoader(),
                new Class<?>[]{InputMessage.class, Request.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "channel" -> channel;
                    case "needReply" -> needReply;
                    default -> defaultValue(method.getReturnType());
                }
        );
        return ServerExchange.of(inputMessage);
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
