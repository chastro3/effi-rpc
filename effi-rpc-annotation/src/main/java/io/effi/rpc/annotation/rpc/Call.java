package io.effi.rpc.annotation.rpc;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures an RPC caller (consumer) method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Call {

    String endpoint() default "";

    String locator() default "";

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
     * Applies filters.
     */
    String[] interceptor() default "";

    /**
     * Registers registries.
     */
    String[] registry() default "";

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
     * Defines failure handle policy.
     */
    String failureHandler() default "";

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



