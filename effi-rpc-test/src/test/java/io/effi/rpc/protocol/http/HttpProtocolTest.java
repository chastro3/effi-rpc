package io.effi.rpc.protocol.http;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.protocol.http.codec.HttpClientCodec;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpDuplexResponse;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.util.TypeCapture;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpProtocolTest {

    @Test
    void errorResponseKeepsRequestMethod() {
        HttpDuplexRequest request = HttpDuplexRequest.builder()
                .version(Http1Protocol.VERSION)
                .method(HttpMethod.POST)
                .url(SmartURL.valueOf("http://127.0.0.1:8080/hello"))
                .headers(List.of())
                .build();

        HttpResponse response = (HttpResponse) new Http1Protocol().createErrorResponse(
                request,
                InteractionErrorCodes.SERVANT_NOT_FOUND.fail("/hello", "127.0.0.1:8080")
        );

        assertEquals(HttpMethod.POST, response.method());
        assertEquals(404, response.statusCode());
        assertEquals("Servant not found for '/hello' on '127.0.0.1:8080'", response.body());
    }

    @Test
    void httpStatusMarksRetryableError() {
        ScopedPlatform platform = new ScopedPlatform("http-error-codec-platform");
        HttpHeaders headers = Http1Protocol.VERSION.newHeaders();
        HttpDuplexResponse response = HttpDuplexResponse.builder()
                .version(Http1Protocol.VERSION)
                .method(HttpMethod.POST)
                .statusCode(HttpResponseStatus.SERVICE_UNAVAILABLE.code())
                .url(SmartURL.valueOf("http://127.0.0.1:8080/hello"))
                .headers(headers)
                .build()
                .channel(channel(platform))
                .input(new ByteArrayInputStream("overloaded".getBytes(StandardCharsets.UTF_8)));
        Caller<?> caller = proxy(Caller.class, (proxy, method, args) -> {
            if ("replyType".equals(method.getName())) {
                return TypeCapture.of(String.class);
            }
            return defaultValue(method.getReturnType());
        });

        HttpResponse decoded = new HttpClientCodec().decode(response, caller);
        EffiRpcException failure = decoded.cause();

        assertEquals(PredefinedErrorCode.SERVICE_UNAVAILABLE, failure.errorCode());
        assertTrue(failure.getMessage().contains("overloaded"));
    }

    private static Channel channel(ScopedPlatform platform) {
        return proxy(Channel.class, (proxy, method, args) -> {
            if ("platform".equals(method.getName())) {
                return platform;
            }
            return defaultValue(method.getReturnType());
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
