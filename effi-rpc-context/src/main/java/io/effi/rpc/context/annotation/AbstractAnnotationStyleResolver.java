package io.effi.rpc.context.annotation;

import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.context.parameter.ParameterBinder;
import io.effi.rpc.context.parameter.ParameterBinding;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Provides an abstract implementation of {@link AnnotationStyleResolver}.
 */
public abstract class AbstractAnnotationStyleResolver<REQ extends Request> implements AnnotationStyleResolver {

    private final AnnotationOptionResolver<Class<?>, ?>[] typeAnnotationOptionResolvers;

    private final AnnotationOptionResolver<Method, ?>[] methodAnnotationOptionResolvers;

    private final AnnotationParameterBinder<?>[] parameterBinders;

    protected AbstractAnnotationStyleResolver() {
        this.typeAnnotationOptionResolvers = typeConfigParsers();
        this.methodAnnotationOptionResolvers = methodConfigParsers();
        this.parameterBinders = parameterBinders();
    }

    @Override
    public HierarchicalOptions resolveType(Class<?> type, HierarchicalOptions options) {
        for (AnnotationOptionResolver<Class<?>, ?> resolver : typeAnnotationOptionResolvers) {
            resolver.resolve(type, options);
        }
        return options;
    }

    @Override
    public HierarchicalOptions resolveMethod(Method method, HierarchicalOptions options) {
        for (AnnotationOptionResolver<Method, ?> parser : methodAnnotationOptionResolvers) {
            parser.resolve(method, options);
        }
        return options;
    }

    @Override
    public MethodBinding resolveMethodBinding(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterBinding[] bindings = new ParameterBinding[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            ParameterBinder binder = null;
            for (AnnotationParameterBinder<?> candidate : parameterBinders) {
                if (candidate.supports(parameter)) {
                    binder = candidate;
                    break;
                }
            }
            if (binder == null) {
                throw new IllegalArgumentException("No parameter binding configured for: " + parameter);
            }
            bindings[i] = new ParameterBinding(i, parameter, binder);
        }
        return MethodBinding.of(method, bindings);
    }

    protected abstract AnnotationOptionResolver<Class<?>, ?>[] typeConfigParsers();

    protected abstract AnnotationOptionResolver<Method, ?>[] methodConfigParsers();

    protected abstract AnnotationParameterBinder<?>[] parameterBinders();

}
