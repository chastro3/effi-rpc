package io.effi.rpc.contract.annotation;

import io.effi.rpc.common.config.DefaultConfigKeys;

import java.lang.annotation.*;

/**
 * RPC caller (consumer) configuration.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface EffiRpcCaller {

    /**
     * Request path.
     *
     * @see DefaultConfigKeys#PATH
     */
    String path() default "";

    /**
     * Annotation style.
     *
     * @see DefaultConfigKeys#ANNOTATION_STYLE
     */
    String style() default "";

    /**
     * Protocol type.
     *
     * @see DefaultConfigKeys#PROTOCOL
     */
    String protocol() default "";

    /**
     * Application name.
     *
     * @see DefaultConfigKeys#APPLICATION
     */
    String application() default "";

    /**
     * Client configuration.
     *
     * @see DefaultConfigKeys#CLIENT_CONFIG
     */
    String clientConfig() default "";

    /**
     * Server address.
     *
     * @see DefaultConfigKeys#ADDRESS
     */
    String address() default "";

    /**
     * List of filters.
     *
     * @see DefaultConfigKeys#FILTERS
     */
    String[] filters() default {};

    /**
     * List of registries.
     *
     * @see DefaultConfigKeys#REGISTRIES
     */
    String[] registries() default {};

    /**
     * Serialization type.
     *
     * @see DefaultConfigKeys#SERIALIZATION
     */
    String serialization() default "";

    /**
     * Compression type.
     *
     * @see DefaultConfigKeys#COMPRESSION
     */
    String compression() default "";

    /**
     * Module name.
     *
     * @see DefaultConfigKeys#MODULE
     */
    String module() default "";

    /**
     * Timeout in milliseconds.
     *
     * @see DefaultConfigKeys#TIMEOUT
     */
    int timeout() default -1;

    /**
     * Retry count.
     *
     * @see DefaultConfigKeys#RETRIES
     */
    int retries() default -1;

    /**
     * Load balancing strategy.
     *
     * @see DefaultConfigKeys#LOAD_BALANCE
     */
    String loadBalance() default "";

    /**
     * Fault tolerance policy.
     *
     * @see DefaultConfigKeys#FAULT_TOLERANCE
     */
    String faultTolerance() default "";

    /**
     * Deserialization threshold.
     *
     * @see DefaultConfigKeys#SERIALIZATION_THRESHOLD
     */
    long serializationThreshold() default -1;

    /**
     * Deserialization threshold.
     *
     * @see DefaultConfigKeys#DESERIALIZATION_THRESHOLD
     */
    long deserializationThreshold() default -1;

    /**
     * Thread pool name.
     *
     * @see DefaultConfigKeys#THREAD_POOL
     */
    String threadPool() default "";
}


