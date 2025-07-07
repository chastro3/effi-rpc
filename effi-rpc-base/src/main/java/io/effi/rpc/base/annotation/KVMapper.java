package io.effi.rpc.base.annotation;

import io.effi.rpc.config.ConfigName;

import java.lang.annotation.Annotation;
import java.util.function.Function;

/**
 * Maps a configuration key to a function that extracts a value from an annotation.
 *
 * @param <T>         the annotation type
 * @param key         the configuration key
 * @param valueGetter the function extracting the value from the annotation
 */
public record KVMapper<T extends Annotation>(ConfigName key, Function<T, String> valueGetter) {}
