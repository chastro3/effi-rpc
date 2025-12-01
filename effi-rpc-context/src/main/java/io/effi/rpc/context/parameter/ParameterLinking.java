package io.effi.rpc.context.parameter;

import io.effi.rpc.context.annotation.AnnotationParameterWrapper;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public record ParameterLinking(Parameter parameter, AnnotationParameterWrapper<?> wrapper) {

    public static ParameterLinking[] emptyWrappers(Method method) {
        Parameter[] parameters = method.getParameters();
        ParameterLinking[] linkings = new ParameterLinking[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            linkings[i] = new ParameterLinking(parameters[i], null);
        }
        return linkings;
    }
}
