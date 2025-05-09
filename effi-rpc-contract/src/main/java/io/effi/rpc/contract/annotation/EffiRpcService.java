package io.effi.rpc.contract.annotation;

import io.effi.rpc.config.DefaultConfigKeys;

import java.lang.annotation.*;

/**
 * Configures an RPC callee (provider) service.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EffiRpcService {

    /**
     * Specifies service name.
     */
    String value() default "";

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
     * Declares supported protocols.
     *
     * @see DefaultConfigKeys#PROTOCOL
     */
    String[] protocol() default {};

    /**
     * Excludes ports.
     *
     * @see DefaultConfigKeys#EXCLUDED_PORT
     */
    int[] excludedPort() default {};

    /**
     * Links associated modules.
     *
     * @see DefaultConfigKeys#MODULES
     */
    String[] modules() default {};

    /**
     * Applies filters.
     *
     * @see DefaultConfigKeys#FILTERS
     */
    String[] filters() default {};

    /**
     * Adds service description.
     *
     * @see DefaultConfigKeys#CALLEE_DESC
     */
    String desc() default "";

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


