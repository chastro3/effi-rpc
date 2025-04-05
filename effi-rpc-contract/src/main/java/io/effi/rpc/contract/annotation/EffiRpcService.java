package io.effi.rpc.contract.annotation;

import io.effi.rpc.common.config.DefaultConfigKeys;

import java.lang.annotation.*;

/**
 * RPC callee (provider) service configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EffiRpcService {

    /**
     * Service name.
     */
    String value() default "";

    /**
     * Request path.
     *
     * @see DefaultConfigKeys#PATH
     */
    String path() default "";

    /**
     * Annotation style.
     *
     * @see DefaultConfigKeys#STYLE
     */
    String style() default "";

    /**
     * Supported protocols.
     *
     * @see DefaultConfigKeys#PROTOCOL
     */
    String[] protocol() default {};

    /**
     * Excluded ports.
     *
     * @see DefaultConfigKeys#EXCLUDED_PORT
     */
    int[] excludedPort() default {};

    /**
     * Associated modules.
     *
     * @see DefaultConfigKeys#MODULES
     */
    String[] modules() default {};

    /**
     * Filters to apply.
     *
     * @see DefaultConfigKeys#FILTERS
     */
    String[] filters() default {};

    /**
     * Description.
     *
     * @see DefaultConfigKeys#CALLEE_DESC
     */
    String desc() default "";

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
     * Serialization threshold.
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

