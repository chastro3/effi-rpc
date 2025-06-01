package io.effi.rpc.annotation.rpc;


import java.lang.annotation.*;

/**
 * Configures an RPC caller (consumer) method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface EffiRpcCaller {

    /**
     * Specifies request path.
     */
    String path() default "";

    /**
     * Defines annotation style.
     */
    String style() default "";

    /**
     * Specifies protocol type.
     */
    String protocol() default "";

    /**
     * Sets remote application name.
     */
    String remoteApplication() default "";

    /**
     * Sets remote module name.
     */
    String remoteModule() default "";

    /**
     * Applies client configuration.
     */
    String clientConfig() default "";

    /**
     * Sets server address.
     */
    String address() default "";

    /**
     * Applies filters.
     */
    String[] filters() default {};

    /**
     * Registers registries.
     */
    String[] registries() default {};

    /**
     * Specifies serialization type.
     */
    String serialization() default "";

    /**
     * Specifies compression type.
     */
    String compression() default "";

    /**
     * Sets module name.
     */
    String module() default "";

    /**
     * Sets timeout in milliseconds.
     */
    int timeout() default -1;

    /**
     * Sets retry count.
     */
    int retries() default -1;

    /**
     * Defines load balancing strategy.
     */
    String loadBalance() default "";

    /**
     * Defines fault tolerance policy.
     */
    String faultTolerance() default "";

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



