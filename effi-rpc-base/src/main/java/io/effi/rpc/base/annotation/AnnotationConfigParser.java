package io.effi.rpc.base.annotation;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.ConfigName;
import io.effi.rpc.util.CollectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Function;

/**
 * Parses the annotation on the specified method and updates the configuration.
 */
public class AnnotationConfigParser<T extends AnnotatedElement, A extends Annotation> {

    private final Class<A> type;

    private final KVMapper<A>[] kvMappers;


    public AnnotationConfigParser(Class<A> type, ConfigName configName, Function<A, String> valueGetter) {
        this(type, new KVMapper<>(configName, valueGetter));
    }

    @SafeVarargs
    public AnnotationConfigParser(Class<A> type, KVMapper<A>... kvMappers) {
        this.type = type;
        this.kvMappers = kvMappers;
    }

    public void parse(T element, Config config) {
        A annotation = element.getAnnotation(this.type);
        if (annotation != null) {
            if (CollectionUtil.isNotEmpty(kvMappers)) {
                for (KVMapper<A> kvMapper : kvMappers) {
                    config.set(kvMapper.key(), kvMapper.valueGetter().apply(annotation));
                }
            }
        }
    }
}