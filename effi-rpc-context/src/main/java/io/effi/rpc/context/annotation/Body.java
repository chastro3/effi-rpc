package io.effi.rpc.context.annotation;

import java.lang.annotation.*;

/**
 * Message body.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Body {}
