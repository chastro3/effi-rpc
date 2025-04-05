package io.effi.rpc.contract.annotation;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.parameter.Argument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Handles wrapping of a method argument into an {@link Argument} based on a specific annotation.
 *
 * @param <T> the annotation type
 */
@FunctionalInterface
public interface AnnotationParameterWrapperHandler<T extends Annotation> {

    /**
     * Wraps the given argument into an {@link Argument} using the provided annotation metadata.
     *
     * @param arg        the argument value
     * @param annotation the annotation instance
     * @param parameter  the method parameter
     * @param caller     the caller context
     * @return the wrapped {@link Argument}, or {@code null} if unsupported
     */
    Argument handle(Object arg, T annotation, Parameter parameter, Caller<?> caller);
}

