package io.effi.rpc.context.annotation;

import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.context.parameter.ParameterLinking;
import io.effi.rpc.context.parameter.ParameterResolver;
import io.effi.rpc.util.CollectionUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * todo 异步解析
 * Provides an abstract implementation of {@link AnnotationStyleResolver}.
 */
public abstract class AbstractAnnotationStyleResolver<REQ extends Request> implements AnnotationStyleResolver {

    protected AnnotationOptionResolver<Class<?>, ?>[] typeAnnotationOptionResolvers;

    protected AnnotationOptionResolver<Method, ?>[] methodAnnotationOptionResolvers;

    protected AnnotationParameterResolver<?, REQ>[] parameterParsers;

    protected AnnotationParameterWrapper<?>[] parameterWrappers;

    @SuppressWarnings("unchecked")
    protected AbstractAnnotationStyleResolver() {
        this.typeAnnotationOptionResolvers = typeConfigParsers().toArray(new AnnotationOptionResolver[0]);
        this.methodAnnotationOptionResolvers = methodConfigParsers().toArray(new AnnotationOptionResolver[0]);
        this.parameterParsers = parameterParsers().toArray(new AnnotationParameterResolver[0]);
        this.parameterWrappers = parameterWrappers().toArray(new AnnotationParameterWrapper[0]);
    }

    @Override
    public HierarchicalOptions resolveType(Class<?> type, HierarchicalOptions options) {
        if (CollectionUtil.isNotEmpty(typeAnnotationOptionResolvers)) {
            for (AnnotationOptionResolver<Class<?>, ?> resolver : typeAnnotationOptionResolvers) {
                resolver.resolve(type, options);
            }
        }
        return options;
    }

    @Override
    public HierarchicalOptions resolveMethod(Method method, HierarchicalOptions options) {
        if (CollectionUtil.isNotEmpty(methodAnnotationOptionResolvers)) {
            for (AnnotationOptionResolver<Method, ?> parser : methodAnnotationOptionResolvers) {
                parser.resolve(method, options);
            }
        }
        return options;
    }

    @Override
    public ParameterLinking[] resolveParameterLinking(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterLinking[] parameterMappers = new ParameterLinking[parameters.length];
        loop:
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (CollectionUtil.isNotEmpty(parameterWrappers)) {
                for (AnnotationParameterWrapper<?> wrapper : parameterWrappers) {
                    if (wrapper.supports(parameter)) {
                        parameterMappers[i] = new ParameterLinking(parameter, wrapper);
                        continue loop;
                    }
                }
            }
            if (parameterMappers[i] == null) {
                parameterMappers[i] = new ParameterLinking(parameter, null);
            }
        }
        return parameterMappers;
    }

    @Override
    public ParameterBinding[] resolveParameterBinding(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterBinding[] bindings = new ParameterBinding[parameters.length];
        loop:
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (CollectionUtil.isNotEmpty(parameterParsers)) {
                for (ParameterResolver<REQ> resolver : parameterParsers) {
                    if (resolver.supports(parameter)) {
                        bindings[i] = new ParameterBinding(parameter, resolver);
                        continue loop;
                    }
                }
            }
            if (bindings[i] == null) {
                bindings[i] = new ParameterBinding(parameter, null);
            }
        }
        return bindings;
    }

    protected abstract List<AnnotationOptionResolver<Class<?>, ?>> typeConfigParsers();

    protected abstract List<AnnotationOptionResolver<Method, ?>> methodConfigParsers();

    protected abstract List<AnnotationParameterWrapper<?>> parameterWrappers();

    protected abstract List<AnnotationParameterResolver<?, REQ>> parameterParsers();

}
