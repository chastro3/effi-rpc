package io.effi.rpc.contract.annotation;

import java.lang.annotation.*;

/**
 * RPC caller (consumer) client configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EffiRpcClient {

    /**
     * Proxy type.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#PROXY
     */
    String proxy() default "";

    /**
     * Request path.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#PATH
     */
    String path() default "";

    /**
     * Annotation style.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#STYLE
     */
    String style() default "";

    /**
     * Protocol type.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#PROTOCOL
     */
    String protocol() default "";

    /**
     * Application name.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#APPLICATION
     */
    String application() default "";

    /**
     * Client configuration.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#CLIENT_CONFIG
     */
    String clientConfig() default "";

    /**
     * Server address.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#ADDRESS
     */
    String address() default "";

    /**
     * List of filters.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#FILTERS
     */
    String[] filters() default {};

    /**
     * List of registries.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#REGISTRIES
     */
    String[] registries() default {};

    /**
     * Serialization type.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#SERIALIZATION
     */
    String serialization() default "";

    /**
     * Compression type.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#COMPRESSION
     */
    String compression() default "";

    /**
     * Module name.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#MODULE
     */
    String module() default "";

    /**
     * Timeout in milliseconds.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#TIMEOUT
     */
    int timeout() default -1;

    /**
     * Retry count.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#RETRIES
     */
    int retries() default -1;

    /**
     * Load balancing strategy.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#LOAD_BALANCE
     */
    String loadBalance() default "";

    /**
     * Fault tolerance policy.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#FAULT_TOLERANCE
     */
    String faultTolerance() default "";

    /**
     * Deserialization threshold.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#SERIALIZATION_THRESHOLD
     */
    long serializationThreshold() default -1;

    /**
     * Deserialization threshold.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#DESERIALIZATION_THRESHOLD
     */
    long deserializationThreshold() default -1;

    /**
     * Thread pool name.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#THREAD_POOL
     */
    String threadPool() default "";
}

