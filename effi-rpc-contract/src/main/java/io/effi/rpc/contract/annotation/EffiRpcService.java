package io.effi.rpc.contract.annotation;

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
     * Supported protocols.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#PROTOCOL
     */
    String[] protocol() default {};

    /**
     * Excluded ports.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#EXCLUDED_PORT
     */
    int[] excludedPort() default {};

    /**
     * Associated modules.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#MODULES
     */
    String[] modules() default {};

    /**
     * Filters to apply.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#FILTERS
     */
    String[] filters() default {};

    /**
     * Description.
     *
     * @see io.effi.rpc.common.constant.DefaultConfigKeys#DESC
     */
    String desc() default "";

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
     * Serialization threshold.
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

