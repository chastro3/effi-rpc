package io.effi.rpc.context.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotation method should be excluded from parsing.
 * <p>
 * When applied, this marker annotation signals that no further annotation-based
 * configuration or processing should be performed on the method.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UnParse {
}
