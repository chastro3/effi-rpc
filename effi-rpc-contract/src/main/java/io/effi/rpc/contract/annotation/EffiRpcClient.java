package io.effi.rpc.contract.annotation;

import io.effi.rpc.config.DefaultConfigKeys;

import java.lang.annotation.*;

/**
 * Configures an RPC caller (consumer) client.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EffiRpcClient {

    /**
     * Specifies proxy type.
     *
     * @see DefaultConfigKeys#PROXY
     */
    String proxy() default "";

    /**
     * Specifies request path.
     *
     * @see DefaultConfigKeys#PATH
     */
    String path() default "";

    /**
     * Defines annotation style.
     *
     * @see DefaultConfigKeys#ANNOTATION_STYLE
     */
    String style() default "";

    /**
     * Specifies protocol type.
     *
     * @see DefaultConfigKeys#PROTOCOL
     */
    String protocol() default "";

    /**
     * Sets application name.
     *
     * @see DefaultConfigKeys#APPLICATION
     */
    String application() default "";

    /**
     * Applies client configuration.
     *
     * @see DefaultConfigKeys#CLIENT_CONFIG
     */
    String clientConfig() default "";

    /**
     * Sets server address.
     *
     * @see DefaultConfigKeys#ADDRESS
     */
    String address() default "";

    /**
     * Applies filters.
     *
     * @see DefaultConfigKeys#FILTERS
     */
    String[] filters() default {};

    /**
     * Registers registries.
     *
     * @see DefaultConfigKeys#REGISTRIES
     */
    String[] registries() default {};

    /**
     * Specifies serialization type.
     *
     * @see DefaultConfigKeys#SERIALIZATION
     */
    String serialization() default "";

    /**
     * Specifies compression type.
     *
     * @see DefaultConfigKeys#COMPRESSION
     */
    String compression() default "";

    /**
     * Sets module name.
     *
     * @see DefaultConfigKeys#MODULE
     */
    String module() default "";

    /**
     * Sets timeout in milliseconds.
     *
     * @see DefaultConfigKeys#TIMEOUT
     */
    int timeout() default -1;

    /**
     * Sets retry count.
     *
     * @see DefaultConfigKeys#RETRIES
     */
    int retries() default -1;

    /**
     * Defines load balancing strategy.
     *
     * @see DefaultConfigKeys#LOAD_BALANCE
     */
    String loadBalance() default "";

    /**
     * Defines fault tolerance policy.
     *
     * @see DefaultConfigKeys#FAULT_TOLERANCE
     */
    String faultTolerance() default "";

    /**
     * Sets serialization threshold.
     *
     * @see DefaultConfigKeys#SERIALIZATION_THRESHOLD
     */
    long serializationThreshold() default -1;

    /**
     * Sets deserialization threshold.
     *
     * @see DefaultConfigKeys#DESERIALIZATION_THRESHOLD
     */
    long deserializationThreshold() default -1;

    /**
     * Specifies thread pool name.
     *
     * @see DefaultConfigKeys#THREAD_POOL
     */
    String threadPool() default "";
}


