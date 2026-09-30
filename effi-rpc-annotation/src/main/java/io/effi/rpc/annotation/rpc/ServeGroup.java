package io.effi.rpc.annotation.rpc;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines a provider-side servant group and its shared defaults.
 * <p>
 * Group-level values apply to every servant in the group. Method-level {@link Serve}
 * declarations override individual operations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServeGroup {

    /**
     * Specifies the service name used for registration and routing.
     */
    String value() default "";

    /**
     * Specifies the business interfaces exposed by the annotated implementation.
     */
    Class<?>[] interfaces() default {};

    /**
     * Specifies the protocols exposed by the servant.
     */
    String[] protocol() default {};

    /**
     * Specifies the serializer extension name.
     */
    String serializer() default "";

    /**
     * Specifies the compressor extension name.
     */
    String compressor() default "";

    /**
     * Specifies the thread pool extension name.
     */
    String threadPool() default "";

    /**
     * Specifies the interceptor names included in the execution chain.
     */
    String[] interceptors() default {};

    /**
     * Specifies the interceptor names excluded from the execution chain.
     */
    String[] excludeInterceptors() default {};

    /**
     * Specifies the associated module name.
     */
    String module() default "";

    /**
     * Specifies the annotation style extension name.
     */
    String annotationStyle() default "";

    /**
     * Specifies the servant label.
     */
    String label() default "";

    /**
     * Specifies the ports excluded from servant exposure.
     */
    int[] excludedPort() default {};

    /**
     * Specifies the serialization threshold used for I/O serialization decisions.
     */
    long serializationThreshold() default -1;

    /**
     * Specifies the deserialization threshold used for I/O deserialization decisions.
     */
    long deserializationThreshold() default -1;
}
