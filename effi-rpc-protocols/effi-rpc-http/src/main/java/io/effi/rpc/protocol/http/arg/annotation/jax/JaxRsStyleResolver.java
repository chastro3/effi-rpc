package io.effi.rpc.protocol.http.arg.annotation.jax;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.annotation.AbstractAnnotationStyleResolver;
import io.effi.rpc.context.annotation.AnnotationOptionResolver;
import io.effi.rpc.context.annotation.AnnotationParameterBinder;
import io.effi.rpc.context.annotation.AnnotationStyleResolver;
import io.effi.rpc.context.annotation.Body;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.options.PeerOptions;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.protocol.http.HttpInvocationKeys;
import io.effi.rpc.protocol.http.HttpOptions;
import io.effi.rpc.protocol.http.arg.binder.HttpBodyParameterBinder;
import io.effi.rpc.protocol.http.arg.binder.HttpHeaderParameterBinder;
import io.effi.rpc.protocol.http.arg.binder.HttpPathParameterBinder;
import io.effi.rpc.protocol.http.arg.binder.HttpQueryParameterBinder;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.util.GenericKey;
import io.netty.handler.codec.http.HttpMethod;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HEAD;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static io.effi.rpc.protocol.http.arg.annotation.jax.JaxRsStyleResolver.NAME;

/**
 * Implements {@link AnnotationStyleResolver} using Jax-rs.
 */
@Extension(value = NAME, onClass = "jakarta.ws.rs.Path")
public class JaxRsStyleResolver extends AbstractAnnotationStyleResolver<HttpRequest> {

    public static final String NAME = "jaxRs";

    @Override
    public boolean supports(Method method) {
        return method.isAnnotationPresent(Path.class);
    }

    @Override
    protected AnnotationParameterBinder<?>[] parameterBinders() {
        return new AnnotationParameterBinder<?>[]{
                new AnnotationParameterBinder<>(
                        PathParam.class,
                        (value, annotation, binding, invocation) ->
                                writeNamed(invocation, HttpInvocationKeys.PATH_VARIABLES, annotation.value(), value),
                        this::readPath
                ),
                new AnnotationParameterBinder<>(
                        QueryParam.class,
                        (value, annotation, binding, invocation) ->
                                writeNamed(invocation, HttpInvocationKeys.QUERY_PARAMETERS, annotation.value(), value),
                        this::readQuery
                ),
                new AnnotationParameterBinder<>(
                        HeaderParam.class,
                        (value, annotation, binding, invocation) ->
                                writeNamed(invocation, HttpInvocationKeys.HEADERS, annotation.value(), value),
                        this::readHeader
                ),
                new AnnotationParameterBinder<>(
                        Body.class,
                        (value, annotation, binding, invocation) ->
                                invocation.set(HttpInvocationKeys.BODY, value),
                        (request, peer, annotation, binding) ->
                                HttpBodyParameterBinder.INSTANCE.resolve(binding, request, peer)
                )
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected AnnotationOptionResolver<Class<?>, ?>[] typeConfigParsers() {
        return new AnnotationOptionResolver[]{
                new AnnotationOptionResolver<>(Path.class, PeerOptions.PATH, path -> new String[]{path.value()})
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    protected AnnotationOptionResolver<Method, ?>[] methodConfigParsers() {
        return new AnnotationOptionResolver[]{
                new AnnotationOptionResolver<>(Path.class, PeerOptions.PATH, path -> new String[]{path.value()}),
                new AnnotationOptionResolver<>(GET.class, HttpOptions.HTTP_METHOD, v -> HttpMethod.GET.name()),
                new AnnotationOptionResolver<>(POST.class, HttpOptions.HTTP_METHOD, v -> HttpMethod.POST.name()),
                new AnnotationOptionResolver<>(PUT.class, HttpOptions.HTTP_METHOD, v -> HttpMethod.PUT.name()),
                new AnnotationOptionResolver<>(DELETE.class, HttpOptions.HTTP_METHOD, v -> HttpMethod.DELETE.name()),
                new AnnotationOptionResolver<>(PATCH.class, HttpOptions.HTTP_METHOD, v -> HttpMethod.PATCH.name()),
                new AnnotationOptionResolver<>(HEAD.class, HttpOptions.HTTP_METHOD, v -> HttpMethod.HEAD.name()),
                new AnnotationOptionResolver<>(OPTIONS.class, HttpOptions.HTTP_METHOD, v -> HttpMethod.OPTIONS.name())
        };
    }

    private void writeNamed(Invocation invocation, GenericKey<Map<String, String>> key, String name, Object value) {
        Map<String, String> values = invocation.computeIfAbsent(key, HashMap::new);
        values.put(name, String.valueOf(value));
    }

    private Object readPath(Request request, Peer peer, PathParam annotation, ParameterBinding binding) {
        return HttpPathParameterBinder.resolveValue(request, peer, annotation.value(), defaultValue(binding));
    }

    private Object readQuery(Request request, Peer peer, QueryParam annotation, ParameterBinding binding) {
        return HttpQueryParameterBinder.resolveValue(request, annotation.value(), defaultValue(binding));
    }

    private Object readHeader(Request request, Peer peer, HeaderParam annotation, ParameterBinding binding) {
        return HttpHeaderParameterBinder.resolveValue(request, annotation.value(), defaultValue(binding));
    }

    private String defaultValue(ParameterBinding binding) {
        DefaultValue annotation = binding.parameter().getAnnotation(DefaultValue.class);
        return annotation == null ? null : annotation.value();
    }

}
