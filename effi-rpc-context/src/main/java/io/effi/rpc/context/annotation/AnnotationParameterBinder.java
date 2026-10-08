package io.effi.rpc.context.annotation;

import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.parameter.ParameterBinder;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.util.AssertUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;

/**
 * Binds a parameter carrying one specific annotation.
 */
public final class AnnotationParameterBinder<T extends Annotation> implements ParameterBinder {

    private final Class<T> type;

    private final Writer<T> writer;

    private final Reader<T> reader;

    public AnnotationParameterBinder(Class<T> type, Writer<T> writer, Reader<T> reader) {
        this.type = AssertUtil.notNull(type, "type");
        this.writer = AssertUtil.notNull(writer, "writer");
        this.reader = AssertUtil.notNull(reader, "reader");
    }

    /**
     * Returns whether the supplied parameter carries the bound annotation.
     *
     * @param parameter reflected parameter
     * @return {@code true} when the annotation is present
     */
    public boolean supports(Parameter parameter) {
        return parameter.isAnnotationPresent(type);
    }

    @Override
    public void write(Object value, ParameterBinding binding, Invocation invocation) {
        T annotation = binding.parameter().getAnnotation(type);
        if (annotation != null) {
            writer.write(value, annotation, binding, invocation);
        }
    }

    @Override
    public Object resolve(ParameterBinding binding, Request request, Peer peer) {
        T annotation = binding.parameter().getAnnotation(type);
        return annotation == null
                ? null
                : reader.read(request, peer, annotation, binding);
    }

    @Override
    public String toString() {
        return "type=" + type.getName();
    }

    /**
     * Writes one annotated parameter value into an invocation.
     *
     * @param <T> annotation type
     */
    @FunctionalInterface
    public interface Writer<T extends Annotation> {

        /**
         * Writes one value into the invocation.
         *
         * @param value      source value
         * @param annotation parameter annotation
         * @param binding    parameter binding
         * @param invocation target invocation
         */
        void write(Object value, T annotation, ParameterBinding binding, Invocation invocation);
    }

    /**
     * Reads one annotated parameter value from a request.
     *
     * @param <T> annotation type
     */
    @FunctionalInterface
    public interface Reader<T extends Annotation> {

        /**
         * Reads one value from the request.
         *
         * @param request    source request
         * @param peer       target peer
         * @param annotation parameter annotation
         * @param binding    parameter binding
         * @return parameter value
         */
        Object read(Request request, Peer peer, T annotation, ParameterBinding binding);
    }
}
