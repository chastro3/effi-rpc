package io.effi.rpc.context.annotation;

import io.effi.rpc.option.OptionName;

import java.lang.annotation.Annotation;
import java.util.function.Function;

/**
 * Maps a configuration key to a function that extracts a value from an annotation.
 *
 * @param <T> annotation type
 * @param <V> option value type
 * @param name option name
 * @param valueGetter function extracting the value from the annotation
 */
public record KVMapper<T extends Annotation, V>(OptionName<V> name, Function<T, V> valueGetter) {}
