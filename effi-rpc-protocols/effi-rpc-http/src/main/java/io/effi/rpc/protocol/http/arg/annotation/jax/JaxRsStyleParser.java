package io.effi.rpc.protocol.http.arg.annotation.jax;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.annotation.AbstractAnnotationStyleParser;
import io.effi.rpc.base.annotation.AnnotationConfigParser;
import io.effi.rpc.base.annotation.AnnotationParameterParser;
import io.effi.rpc.base.annotation.AnnotationParameterWrapper;
import io.effi.rpc.base.annotation.AnnotationStyleParser;
import io.effi.rpc.base.annotation.Body;
import io.effi.rpc.base.parameter.Argument;
import io.effi.rpc.base.parameter.Header;
import io.effi.rpc.base.parameter.ParamVar;
import io.effi.rpc.base.parameter.PathVar;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.netty.buffer.ByteBuf;
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

import static io.effi.rpc.constant.Component.AnnotationStyle.JAX_RS;

/**
 * Implements {@link AnnotationStyleParser} using Jax-rs.
 */
@Extension(value = JAX_RS, onClass = "jakarta.ws.rs.Path")
public class JaxRsStyleParser extends AbstractAnnotationStyleParser<HttpRequest<ByteBuf>> {

    @Override
    public boolean supported(Method method) {
        return method.isAnnotationPresent(Path.class);
    }

    @Override
    protected List<AnnotationParameterParser<?, HttpRequest<ByteBuf>>> parameterParsers() {
        return List.of(
                new AnnotationParameterParser<>(PathParam.class, this::getPathOrDefault),
                new AnnotationParameterParser<>(QueryParam.class, this::getParamOrDefault),
                new AnnotationParameterParser<>(HeaderParam.class, this::getHeaderOrDefault),
                new AnnotationParameterParser<>(Body.class, HttpUtil::getBody)
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
    protected List<AnnotationConfigParser<Class<?>, ?>> typeConfigParsers() {
        return List.of(
                new AnnotationConfigParser<>(Path.class, DefaultConfigNames.PATH, Path::value)
        );
    }

    @Override
    protected List<AnnotationConfigParser<Method, ?>> methodConfigParsers() {
        return List.of(
                new AnnotationConfigParser<>(Path.class, DefaultConfigNames.PATH, Path::value),
                new AnnotationConfigParser<>(GET.class, DefaultConfigNames.HTTP_METHOD, v -> HttpMethod.GET.name()),
                new AnnotationConfigParser<>(POST.class, DefaultConfigNames.HTTP_METHOD, v -> HttpMethod.POST.name()),
                new AnnotationConfigParser<>(PUT.class, DefaultConfigNames.HTTP_METHOD, v -> HttpMethod.PUT.name()),
                new AnnotationConfigParser<>(DELETE.class, DefaultConfigNames.HTTP_METHOD, v -> HttpMethod.DELETE.name()),
                new AnnotationConfigParser<>(PATCH.class, DefaultConfigNames.HTTP_METHOD, v -> HttpMethod.PATCH.name()),
                new AnnotationConfigParser<>(HEAD.class, DefaultConfigNames.HTTP_METHOD, v -> HttpMethod.HEAD.name()),
                new AnnotationConfigParser<>(OPTIONS.class, DefaultConfigNames.HTTP_METHOD, v -> HttpMethod.OPTIONS.name())
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

    private Object getPathOrDefault(HttpRequest<ByteBuf> request, PathParam pathParam, AnnotatedElement element, Callee callee) {
        return getParameterOfDefault(element, () -> HttpUtil.findPathForVar(request.url(), pathParam.value(), callee));
    }

    private Object getParamOrDefault(HttpRequest<ByteBuf> request, QueryParam queryParam, AnnotatedElement element, Callee callee) {
        return getParameterOfDefault(element, () -> HttpUtil.findParamForVar(request.url(), queryParam.value()));
    }

    private Object getHeaderOrDefault(HttpRequest<ByteBuf> request, HeaderParam headerParam, AnnotatedElement element, Callee callee) {
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
