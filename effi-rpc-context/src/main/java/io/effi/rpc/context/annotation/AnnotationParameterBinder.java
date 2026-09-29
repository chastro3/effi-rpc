package io.effi.rpc.context.annotation;

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

    @Override
    public void bind(Object value, ParameterBinding binding, Invocation invocation) {
        T annotation = binding.parameter().getAnnotation(type);
        if (annotation != null) {
            writer.write(value, annotation, binding, invocation);
        }
    }

    @Override
    public Object resolve(ParameterBinding binding, Invocation invocation) {
        T annotation = binding.parameter().getAnnotation(type);
        return annotation == null
                ? null
                : reader.read(invocation, annotation, binding);
    }

    public boolean supports(Parameter parameter) {
        return parameter.isAnnotationPresent(type);
    }

    @Override
    public String toString() {
        return "type=" + type.getName();
    }

    @FunctionalInterface
    public interface Writer<T extends Annotation> {

        void write(Object value, T annotation, ParameterBinding binding, Invocation invocation);
    }

    @FunctionalInterface
    public interface Reader<T extends Annotation> {

        Object read(Invocation invocation, T annotation, ParameterBinding binding);
    }
}
