package io.effi.rpc.context.annotation;

import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.ServantGroup;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.trait.Builder;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

/**
 * Builds an annotation callee instance.
 */
public class AnnotationCalleeBuilder<S> {

    private final ServantGroup<S> servantGroup;

    private final Method method;

    private String style;

    public AnnotationCalleeBuilder(ServantGroup<S> servantGroup, String methodName, Class<?>... parameterTypes) {
        this.servantGroup = AssertUtil.notNull(servantGroup, "remoteService");
        try {
            this.method = servantGroup.targetType().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Can't find method: " + methodName + " in " + servantGroup.targetType());
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
    public <T extends Servant> T build(BiFunction<ServantMethod<S>, HierarchicalOptions, Builder<T>> builder) {
        AssertUtil.notBlank(style, "style");
        AnnotationStyleResolver styleResolver = AnnotationStyle.getInstance(style).resolver();
        HierarchicalOptions options = HierarchicalOptions.create().withOwner(servantGroup);
        styleResolver.resolveType(servantGroup.targetType(), options);
        ParameterBinding[] bindings = styleResolver.resolveParameterBinding(method);
        ServantMethod<S> servantMethod = new ServantMethod<>(servantGroup, method, bindings);
        HierarchicalOptions serveOptions = styleResolver.resolveMethod(method, HierarchicalOptions.create());
        return builder.apply(servantMethod, serveOptions).build();
    }
}

