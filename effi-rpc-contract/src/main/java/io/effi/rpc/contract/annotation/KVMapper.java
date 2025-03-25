package io.effi.rpc.contract.annotation;

import io.effi.rpc.common.url.ConfigKey;

import java.lang.annotation.Annotation;
import java.util.function.Function;

/**
 * Maps a configuration key to a function that extracts a value from an annotation.
 *
 * @param <T>         the annotation type
 * @param key         the configuration key
 * @param valueGetter the function extracting the value from the annotation
 */
public record KVMapper<T extends Annotation>(ConfigKey key, Function<T, String> valueGetter) {}
