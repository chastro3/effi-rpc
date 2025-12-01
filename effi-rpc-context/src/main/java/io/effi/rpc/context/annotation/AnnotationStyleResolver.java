package io.effi.rpc.context.annotation;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.context.parameter.ParameterLinking;

import java.lang.reflect.Method;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Resolves annotation-based classes.
 */
@Extensible(scope = PLATFORM)
public interface AnnotationStyleResolver {

    /**
     * Resolves annotations on the class.
     *
     * @param type the class to be parsed
     * @param config the options
     */
    HierarchicalOptions resolveType(Class<?> type, HierarchicalOptions options);

    /**
     * Resolves annotations on the method.
     *
     * @param method the method to be parsed
     * @param config the options
     * @return updated options
     */
    HierarchicalOptions resolveMethod(Method method, HierarchicalOptions options);

    /**
     * Resolves the caller's parameter mapping.
     *
     * @param method the method for parameter mapping
     * @return an array of parameter mappers
     */
    ParameterLinking[] resolveParameterLinking(Method method);

    /**
     * Resolves the servant's parameter binding from method.
     *
     * @param method the method for parameter mapping
     * @return an array of parameter bindings
     */
    ParameterBinding[] resolveParameterBinding(Method method);

    /**
     * Checks if the method is supported.
     *
     * @param method the method to check
     * @return true if supported, false otherwise
     */
    boolean supports(Method method);
}



