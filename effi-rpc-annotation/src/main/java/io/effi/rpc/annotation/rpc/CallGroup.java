package io.effi.rpc.annotation.rpc;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures an RPC caller (consumer) client.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CallGroup {

    /**
     * Specifies proxy type.
     */
    String proxy() default "";

    Call call() default @Call;

}


