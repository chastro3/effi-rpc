package io.effi.rpc.base.annotation;

import java.lang.annotation.*;

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
