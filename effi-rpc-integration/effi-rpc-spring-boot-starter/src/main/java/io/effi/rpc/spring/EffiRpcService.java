package io.effi.rpc.spring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Spring bean as an RPC provider implementation.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EffiRpcService {

    Class<?>[] interfaces() default {};

    String[] protocols() default {};

    String server() default "";

    String serializer() default "";

    String compression() default "";

    String threadPool() default "";

    String module() default "";
}
