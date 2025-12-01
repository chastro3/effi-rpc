package io.effi.rpc.annotation.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures an RPC servant (provider) service.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServeGroup {

    /**
     * Specifies service name.
     */
    String value() default "";

    Serve serve() default @Serve;
}


