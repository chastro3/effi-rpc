package io.effi.rpc.protocol.http;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.parameter.MethodBinder;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.protocol.http.arg.binder.HttpPathParameterBinder;
import io.effi.rpc.protocol.http.arg.binder.HttpQueryParameterBinder;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.MediaType;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.serialization.json.JacksonSerializer;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class HttpCallContextResolverTest {

    private static final AtomicInteger PLATFORM_IDS = new AtomicInteger();

    @Test
    void resolvesNamedHttpParameters() throws Exception {
        Method method = Sample.class.getDeclaredMethod("find", String.class, int.class);
        MethodBinding binding = MethodBinding.of(method, new ParameterBinding[]{
                new ParameterBinding(0, method.getParameters()[0], new HttpPathParameterBinder("id", null)),
                new ParameterBinding(1, method.getParameters()[1], new HttpQueryParameterBinder("age", null))
        });
        HttpDuplexRequest request = request(
                SmartURL.valueOf("http://127.0.0.1:8080/users/42?age=18"),
                null
        );
        Servant servant = servant(binding, QueryPath.valueOf("/users/{id}"), null, null);

        CallContext<Request, Servant> context = new HttpCallContextResolver().resolve(request, servant);

        assertArrayEquals(new Object[]{"42", 18}, context.args());
    }

    private static HttpDuplexRequest request(SmartURL url, byte[] body) {
        HttpHeaders headers = Http1Protocol.VERSION.newHeaders();
        headers.set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON.contentType());
        HttpDuplexRequest request = HttpDuplexRequest.builder()
                .version(Http1Protocol.VERSION)
                .method(HttpMethod.POST)
                .url(url)
                .headers(headers)
                .build();
        return body == null ? request : request.input(new ByteArrayInputStream(body));
    }

    @SuppressWarnings("unchecked")
    private static Servant servant(
            MethodBinding binding,
            QueryPath queryPath,
            ScopedPlatform platform,
            ScopedModule module
    ) {
        MethodBinder binder = new MethodBinder(binding);
        return (Servant) Proxy.newProxyInstance(
                HttpCallContextResolverTest.class.getClassLoader(),
                new Class<?>[]{Servant.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "methodBinder" -> binder;
                    case "queryPath" -> queryPath;
                    case "platform" -> platform;
                    case "module" -> module;
                    case "toString" -> "test-servant";
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    default -> null;
                }
        );
    }

    @Test
    void resolvesMultiplePositionalValuesFromJson() throws Exception {
        Method method = Sample.class.getDeclaredMethod("hello", String.class, int.class);
        MethodBinding binding = MethodBinding.positional(method);
        JacksonSerializer serializer = new JacksonSerializer();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        serializer.serialize(new Object[]{"tom", 18}, output);
        HttpDuplexRequest request = request(
                SmartURL.valueOf("http://127.0.0.1:8080/hello"),
                output.toByteArray()
        );
        ScopedPlatform platform = platform(serializer);
        ScopedModule module = platform.newApplication().newModule();
        Servant servant = servant(binding, QueryPath.empty(), platform, module);

        CallContext<Request, Servant> context = new HttpCallContextResolver().resolve(request, servant);

        assertArrayEquals(new Object[]{"tom", 18}, context.args());
    }

    private static ScopedPlatform platform(Serializer serializer) {
        ScopedPlatform platform = new ScopedPlatform("http-resolver-" + PLATFORM_IDS.incrementAndGet());
        platform.registry().register(Serializer.class, JacksonSerializer.NAME, serializer);
        return platform;
    }

    private interface Sample {

        String hello(String name, int age);

        String find(String id, int age);
    }
}
