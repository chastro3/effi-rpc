package io.effi.rpc.context.annotation;

import io.effi.rpc.config.OptionName;

import java.lang.annotation.Annotation;
import java.util.function.Function;

/**
 * Maps a configuration key to a function that extracts a value from an annotation.
 *
 * @param <T>         the annotation type
 * @param key         the option key
 * @param valueGetter the function extracting the value from the annotation
 */
public record KVMapper<T extends Annotation, V>(OptionName<V> name, Function<T, V> valueGetter) {}
