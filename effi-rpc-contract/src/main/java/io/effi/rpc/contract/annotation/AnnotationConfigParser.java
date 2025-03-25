package io.effi.rpc.contract.annotation;

import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.ConfigKey;
import io.effi.rpc.common.util.CollectionUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Function;

public class AnnotationConfigParser<T extends AnnotatedElement, A extends Annotation> {

    private final Class<A> type;

    private final KVMapper<A>[] kvMappers;

    /**
     * Creates a parser with a single configuration mapping.
     *
     * @param type        the annotation type to parameter
     * @param configKey   the configuration key to update
     * @param valueGetter a function to extract the configuration value
     */
    public AnnotationConfigParser(Class<A> type, ConfigKey configKey, Function<A, String> valueGetter) {
        this(type, new KVMapper<>(configKey, valueGetter));
    }

    /**
     * Creates a parser with multiple configuration mappings.
     *
     * @param type      the annotation type to parameter
     * @param kvMappers mappings defining how to extract configuration values
     */
    @SafeVarargs
    public AnnotationConfigParser(Class<A> type, KVMapper<A>... kvMappers) {
        this.type = type;
        this.kvMappers = kvMappers;
    }

    /**
     * Parses the annotation on the specified method and updates the configuration.
     *
     * @param element the element to parameter
     * @param config  the configuration to update
     */
    public void parse(T element, Config config) {
        A annotation = element.getAnnotation(this.type);
        if (annotation != null) {
            if (CollectionUtil.isNotEmpty(kvMappers)) {
                for (KVMapper<A> kvMapper : kvMappers) {
                    config.set(kvMapper.key().key(), kvMapper.valueGetter().apply(annotation));
                }
            }
        }
    }
}