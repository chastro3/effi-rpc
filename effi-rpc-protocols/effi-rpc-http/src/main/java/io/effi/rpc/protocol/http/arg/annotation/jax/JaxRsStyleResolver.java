package io.effi.rpc.protocol.http.arg.annotation.jax;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.annotation.AbstractAnnotationStyleResolver;
import io.effi.rpc.context.annotation.AnnotationOptionResolver;
import io.effi.rpc.context.annotation.AnnotationParameterResolver;
import io.effi.rpc.context.annotation.AnnotationParameterWrapper;
import io.effi.rpc.context.annotation.AnnotationStyleResolver;
import io.effi.rpc.context.annotation.Body;
import io.effi.rpc.context.parameter.Argument;
import io.effi.rpc.context.parameter.Header;
import io.effi.rpc.context.parameter.ParamVar;
import io.effi.rpc.context.parameter.PathVar;
import io.effi.rpc.protocol.http.HttpProtocol;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

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
    protected List<AnnotationParameterResolver<?, HttpRequest>> parameterParsers() {
        return List.of(
                new AnnotationParameterResolver<>(PathParam.class, this::getPathOrDefault),
                new AnnotationParameterResolver<>(QueryParam.class, this::getParamOrDefault),
                new AnnotationParameterResolver<>(HeaderParam.class, this::getHeaderOrDefault),
                new AnnotationParameterResolver<>(Body.class, HttpUtil::getBody)
        );
    }

    @Override
    protected List<AnnotationParameterWrapper<?>> parameterWrappers() {
        return List.of(
                new AnnotationParameterWrapper<>(PathParam.class, this::wrapPath),
                new AnnotationParameterWrapper<>(QueryParam.class, this::wrapParam),
                new AnnotationParameterWrapper<>(HeaderParam.class, this::wrapHeader),
                AnnotationParameterWrapper.BODY_WRAPPER
        );
    }

    @Override
    protected List<AnnotationOptionResolver<Class<?>, ?>> typeConfigParsers() {
        return List.of(
                new AnnotationOptionResolver<>(Path.class, Peer.PATH, Path::value)
        );
    }

    @Override
    protected List<AnnotationOptionResolver<Method, ?>> methodConfigParsers() {
        return List.of(
                new AnnotationOptionResolver<>(Path.class, Peer.PATH, Path::value),
                new AnnotationOptionResolver<>(GET.class, HttpProtocol.HTTP_METHOD, v -> HttpMethod.GET.name()),
                new AnnotationOptionResolver<>(POST.class, HttpProtocol.HTTP_METHOD, v -> HttpMethod.POST.name()),
                new AnnotationOptionResolver<>(PUT.class, HttpProtocol.HTTP_METHOD, v -> HttpMethod.PUT.name()),
                new AnnotationOptionResolver<>(DELETE.class, HttpProtocol.HTTP_METHOD, v -> HttpMethod.DELETE.name()),
                new AnnotationOptionResolver<>(PATCH.class, HttpProtocol.HTTP_METHOD, v -> HttpMethod.PATCH.name()),
                new AnnotationOptionResolver<>(HEAD.class, HttpProtocol.HTTP_METHOD, v -> HttpMethod.HEAD.name()),
                new AnnotationOptionResolver<>(OPTIONS.class, HttpProtocol.HTTP_METHOD, v -> HttpMethod.OPTIONS.name())
        );
    }

    private Argument wrapPath(Object arg, PathParam pathParam, Parameter parameter, Caller<?> caller) {
        return PathVar.target(Map.of(pathParam.value(), String.valueOf(arg)));
    }

    private Argument wrapParam(Object arg, QueryParam queryParam, Parameter parameter, Caller<?> caller) {
        return ParamVar.target(Map.of(queryParam.value(), String.valueOf(arg)));
    }

    private Argument wrapHeader(Object arg, HeaderParam headerParam, Parameter parameter, Caller<?> caller) {
        return Header.target(Map.of(headerParam.value(), String.valueOf(arg)));
    }

    private Object getPathOrDefault(HttpRequest request, PathParam pathParam, AnnotatedElement element, Servant servant) {
        return getParameterOfDefault(element, () -> HttpUtil.findPathForVar(request.url(), pathParam.value(), servant));
    }

    private Object getParamOrDefault(HttpRequest request, QueryParam queryParam, AnnotatedElement element, Servant servant) {
        return getParameterOfDefault(element, () -> request.url().getQueryParam(queryParam.value()));
    }

    private Object getHeaderOrDefault(HttpRequest request, HeaderParam headerParam, AnnotatedElement element, Servant servant) {
        return getParameterOfDefault(element, () -> request.headers().get(headerParam.value()));
    }

    private Object getParameterOfDefault(AnnotatedElement element, Supplier<?> valueGetter) {
        Object value = valueGetter.get();
        return getValueOrDefault(value, element);
    }

    private Object getValueOrDefault(Object value, AnnotatedElement element) {
        if (value == null) {
            DefaultValue defaultValue = element.getAnnotation(DefaultValue.class);
            if (defaultValue != null) {
                return defaultValue.value();
            }
        }
        return value;
    }

}
