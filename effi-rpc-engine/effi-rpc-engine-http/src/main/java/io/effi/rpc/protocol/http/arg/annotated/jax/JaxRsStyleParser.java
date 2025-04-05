package io.effi.rpc.protocol.http.arg.annotated.jax;

import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.spi.Extension;
import io.effi.rpc.contract.annotation.*;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import jakarta.ws.rs.*;

import java.lang.reflect.Method;
import java.util.List;

import static io.effi.rpc.common.constant.Component.AnnotationStyle.JAX_RS;

/**
 * {@link AnnotationStyleParser} implementation based on jax-rs.
 */
@Extension(value = JAX_RS, onClass = "jakarta.ws.rs.core.Application")
public class JaxRsStyleParser extends AbstractAnnotationStyleParser<HttpRequest<ByteBuf>> {

    @Override
    public boolean supported(Method method) {
        return method.isAnnotationPresent(Path.class);
    }

    @Override
    protected List<AnnotationParameterParser<?, HttpRequest<ByteBuf>>> parameterParsers() {
        return List.of(
                new AnnotationParameterParser<>(PathParam.class, JaxRsUtil::getPathOrDefault),
                new AnnotationParameterParser<>(QueryParam.class, JaxRsUtil::getParamOrDefault),
                new AnnotationParameterParser<>(HeaderParam.class, JaxRsUtil::getHeaderOrDefault),
                new AnnotationParameterParser<>(Body.class, HttpUtil::getBody)
        );
    }

    @Override
    protected List<AnnotationParameterWrapper<?>> parameterWrappers() {
        return List.of(
                new AnnotationParameterWrapper<>(PathParam.class, JaxRsUtil::wrapPath),
                new AnnotationParameterWrapper<>(QueryParam.class, JaxRsUtil::wrapParam),
                new AnnotationParameterWrapper<>(HeaderParam.class, JaxRsUtil::wrapHeader),
                AnnotationParameterWrapper.BODY_WRAPPER
        );
    }

    @Override
    protected List<AnnotationConfigParser<Class<?>, ?>> typeConfigParsers() {
        return List.of(
                new AnnotationConfigParser<>(Path.class, DefaultConfigKeys.PATH, Path::value)
        );
    }

    @Override
    protected List<AnnotationConfigParser<Method, ?>> methodConfigParsers() {
        return List.of(
                new AnnotationConfigParser<>(Path.class, DefaultConfigKeys.PATH, Path::value),
                new AnnotationConfigParser<>(GET.class, DefaultConfigKeys.HTTP_METHOD, v -> HttpMethod.GET.name()),
                new AnnotationConfigParser<>(POST.class, DefaultConfigKeys.HTTP_METHOD, v -> HttpMethod.POST.name()),
                new AnnotationConfigParser<>(PUT.class, DefaultConfigKeys.HTTP_METHOD, v -> HttpMethod.PUT.name()),
                new AnnotationConfigParser<>(DELETE.class, DefaultConfigKeys.HTTP_METHOD, v -> HttpMethod.DELETE.name()),
                new AnnotationConfigParser<>(PATCH.class, DefaultConfigKeys.HTTP_METHOD, v -> HttpMethod.PATCH.name()),
                new AnnotationConfigParser<>(HEAD.class, DefaultConfigKeys.HTTP_METHOD, v -> HttpMethod.HEAD.name()),
                new AnnotationConfigParser<>(OPTIONS.class, DefaultConfigKeys.HTTP_METHOD, v -> HttpMethod.OPTIONS.name())
        );
    }

}
