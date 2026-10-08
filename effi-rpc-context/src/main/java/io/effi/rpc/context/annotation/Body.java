package io.effi.rpc.context.annotation;

import java.lang.annotation.*;

/**
 * Marks a parameter as the RPC message body.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Body {}
