package io.effi.rpc.annotation.rpc;

import java.lang.annotation.*;

/**
 * Configures an RPC callee (provider) method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface EffiRpcCallee {

    /**
     * Specifies request path.
     */
    String path() default "";

    /**
     * Defines annotation style.
     */
    String style() default "";

    /**
     * Declares supported protocols.
     */
    String[] protocol() default {};

    /**
     * Lists excluded ports.
     */
    int[] excludedPort() default {};

    /**
     * Sets module name.
     */
    String module() default "";

    /**
     * Applies filters.
     */
    String[] filters() default {};

    /**
     * Describes callee.
     */
    String desc() default "";

    /**
     * Specifies serialization type.
     */
    String serialization() default "";

    /**
     * Specifies compression type.
     */
    String compression() default "";

    /**
     * Sets serialization threshold.
     */
    long serializationThreshold() default -1;

    /**
     * Sets deserialization threshold.
     */
    long deserializationThreshold() default -1;

    /**
     * Specifies thread pool name.
     */
    String threadPool() default "";
}



