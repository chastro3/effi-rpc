package io.effi.rpc.contract.annotation;

import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.RemoteService;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.contract.parameter.ParameterParser;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.Builder;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * Builds an annotation callee instance.
 */
public class AnnotationCalleeBuilder<S> {

    private final RemoteService<S> remoteService;

    private final Method method;

    private String style;

    public AnnotationCalleeBuilder(RemoteService<S> remoteService, String methodName, Class<?>... parameterTypes) {
        this.remoteService = AssertUtil.notNull(remoteService, "remoteService");
        try {
            this.method = remoteService.serviceType().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Can't find method: " + methodName + " in " + remoteService.serviceType());
        }
    }

    /**
     * Sets the style for method annotation parsing.
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
    public <T extends Callee<S>> T build(BiFunction<MethodMapper<S>, NodeConfig, Builder<T>> builder) {
        AssertUtil.notBlank(style, "style");
        AnnotationStyleParser methodParser = ExtensionLoader.loadExtension(AnnotationStyleParser.class, style);
        ParameterMapper<ParameterParser<?>>[] parameterMappers = methodParser.parseCalleeParameterMapper(method);
        MethodMapper<S> methodMapper = new MethodMapper<>(remoteService, method, parameterMappers);
        NodeConfig config = methodParser.parseMethod(method, new HierarchicalNodeConfig());
        return builder.apply(methodMapper, config).build();
    }
}

