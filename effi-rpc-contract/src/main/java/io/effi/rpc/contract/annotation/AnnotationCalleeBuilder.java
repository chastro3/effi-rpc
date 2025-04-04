package io.effi.rpc.contract.annotation;

import io.effi.rpc.common.util.Builder;
import io.effi.rpc.common.spi.ExtensionLoader;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.RemoteService;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.contract.parameter.ParameterParser;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * Builder class for creating an annotation callee instance.
 *
 * @param <S> the type of the remote service
 */
public class AnnotationCalleeBuilder<S> {

    private final RemoteService<S> remoteService;

    private final Method method;

    private String style;

    /**
     * Constructs an AnnotatedCalleeBuilder with the given remote service and method details.
     *
     * @param remoteService  the remote service instance
     * @param methodName     the name of the method to be invoked
     * @param parameterTypes the parameter types of the method
     * @throws IllegalArgumentException if the method cannot be found in the remote service
     */
    public AnnotationCalleeBuilder(RemoteService<S> remoteService, String methodName, Class<?>... parameterTypes) {
        this.remoteService = AssertUtil.notNull(remoteService, "remoteService");
        try {
            this.method = remoteService.targetType().getMethod(methodName, parameterTypes); // Get method by name and types
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Can't find method: " + methodName + " in " + remoteService.targetType());
        }
    }

    /**
     * Sets the style for method annotation parsing.
     *
     * @param style the style to be used for annotation parsing
     * @return the current AnnotatedCalleeBuilder instance for chaining
     */
    public AnnotationCalleeBuilder<S> useStyle(String style) {
        this.style = style;
        return this;
    }

    /**
     * Builds the Callee instance using the specified builder.
     *
     * @param builder a function that constructs a Callee instance using a method mapper and configuration
     * @param <T>     the type of the Callee
     * @return the constructed Callee instance
     * @throws IllegalArgumentException if the style is not set or is invalid
     */
    public <T extends Callee<S>> T build(BiFunction<MethodMapper<S>, Config, Builder<T>> builder) {
        AssertUtil.notBlank(style, "style");
        AnnotationStyleParser methodParser = ExtensionLoader.loadExtension(AnnotationStyleParser.class, style);
        ParameterMapper<ParameterParser<?>>[] parameterMappers = methodParser.parseCalleeParameterMapper(method);
        MethodMapper<S> methodMapper = new MethodMapper<>(remoteService, method, parameterMappers);
        Config config = methodParser.parseMethod(method, new Config());
        return builder.apply(methodMapper, config).build();
    }
}

