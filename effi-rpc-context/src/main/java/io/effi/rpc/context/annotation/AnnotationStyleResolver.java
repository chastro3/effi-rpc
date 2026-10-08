package io.effi.rpc.context.annotation;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.context.parameter.MethodBinding;

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
     * @param options target options
     * @return updated options
     */
    HierarchicalOptions resolveType(Class<?> type, HierarchicalOptions options);

    /**
     * Resolves annotations on the method.
     *
     * @param method the method to be parsed
     * @param options target options
     * @return updated options
     */
    HierarchicalOptions resolveMethod(Method method, HierarchicalOptions options);

    /**
     * Resolves all parameter bindings for the method.
     *
     * @param method the method for parameter binding
     * @return the method binding
     */
    MethodBinding resolveMethodBinding(Method method);

    /**
     * Checks if the method is supported.
     *
     * @param method the method to check
     * @return true if supported, false otherwise
     */
    boolean supports(Method method);
}



