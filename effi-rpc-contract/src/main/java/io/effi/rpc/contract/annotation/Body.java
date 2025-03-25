package io.effi.rpc.contract.annotation;

import java.lang.annotation.*;

/**
 * Message body.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Body {}
