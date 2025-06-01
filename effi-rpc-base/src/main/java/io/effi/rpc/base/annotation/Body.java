package io.effi.rpc.base.annotation;

import java.lang.annotation.*;

/**
 * Message body.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Body {}
