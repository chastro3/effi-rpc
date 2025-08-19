package io.effi.rpc.context.annotation;

import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.parameter.Argument;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Wraps a method argument into an {@link Argument} based on a specific annotation.
 */
public class AnnotationParameterWrapper<T extends Annotation> {

    public static final AnnotationParameterWrapper<Body> BODY_WRAPPER = new AnnotationParameterWrapper<>(Body.class,
            (arg, annotation, parameter, caller) -> io.effi.rpc.context.parameter.Body.wrap(arg)
    );

    private final Class<T> type;

    private final AnnotationParameterWrapperHandler<T> handler;

    public AnnotationParameterWrapper(Class<T> type, AnnotationParameterWrapperHandler<T> handler) {
        this.type = AssertUtil.notNull(type, "type");
        this.handler = AssertUtil.notNull(handler, "handler");
    }

    public Argument wrap(Object arg, Parameter parameter, Caller<?> caller) {
        T annotation = parameter.getAnnotation(type);
        if (annotation != null) {
            return handler.handle(arg, annotation, parameter, caller);
        }
        return null;
    }

    public boolean supported(Parameter parameter) {
        return parameter.isAnnotationPresent(type);
    }

    @Override
    public String toString() {
        return "type=" + type.getName();
    }

}
