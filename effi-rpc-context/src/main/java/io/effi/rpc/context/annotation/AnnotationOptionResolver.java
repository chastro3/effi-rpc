package io.effi.rpc.context.annotation;

import io.effi.rpc.option.OptionName;
import io.effi.rpc.option.Options;
import io.effi.rpc.util.CollectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Function;

/**
 * Resolves the annotation on the specified method and updates the configuration.
 */
public class AnnotationOptionResolver<T extends AnnotatedElement, A extends Annotation> {

    private final Class<A> type;

    private final KVMapper<A, ?>[] kvMappers;


    public <V> AnnotationOptionResolver(Class<A> type, OptionName<V> name, Function<A, V> valueGetter) {
        this(type, new KVMapper<>(name, valueGetter));
    }

    @SafeVarargs
    public AnnotationOptionResolver(Class<A> type, KVMapper<A, ?>... kvMappers) {
        this.type = type;
        this.kvMappers = kvMappers;
    }

    /**
     * Resolves the bound annotation and applies its options.
     *
     * @param element annotated element
     * @param options target options
     */
    public void resolve(T element, Options options) {
        A annotation = element.getAnnotation(this.type);
        if (annotation != null) {
            if (CollectionUtil.isNotEmpty(kvMappers)) {
                for (KVMapper<A, ?> kvMapper : kvMappers) {
                    resolveOption(options, annotation, kvMapper);
                }
            }
        }
    }

    private <V> void resolveOption(Options options, A annotation, KVMapper<A, V> mapper) {
        options.addOption(mapper.name(), mapper.valueGetter().apply(annotation));
    }
}
