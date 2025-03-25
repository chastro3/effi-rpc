package io.effi.rpc.contract.annotation;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.parameter.Argument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

@FunctionalInterface
public interface AnnotationParameterWrapperHandler<T extends Annotation> {

    Argument handle(Object arg, T annotation, Parameter parameter, Caller<?> caller);
}
