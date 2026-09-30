package io.effi.rpc.annotation.rpc;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines provider-side method contract and servant overrides.
 * <p>
 * A method-level declaration overrides the group defaults supplied by {@link ServeGroup#serve()}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Serve {

    /**
     * Specifies the method path.
     */
    String path() default "";

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

