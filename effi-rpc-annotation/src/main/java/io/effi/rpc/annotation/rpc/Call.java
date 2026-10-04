package io.effi.rpc.annotation.rpc;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines consumer-side call contract and caller overrides.
 * <p>
 * A method-level declaration overrides the group defaults supplied by {@link CallGroup#call()}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Call {

    /**
     * Specifies the method path.
     */
    String path() default "";

    /**
     * Specifies the transport protocol extension name.
     */
    String protocol() default "";

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
     * Specifies the locator extension name.
     */
    String locator() default "";

    /**
     * Specifies the registry configuration names.
     */
    String[] registry() default {};

    /**
     * Specifies the load balancer extension name.
     */
    String loadBalancer() default "";

    /**
     * Specifies the router extension name.
     */
    String router() default "";

    /**
     * Specifies the service discovery extension name.
     */
    String serviceDiscovery() default "";

    /**
     * Specifies the positional argument index used as the consistent-hash key.
     */
    int hashKeyIndex() default -1;

    /**
     * Specifies the failure handler extension name.
     */
    String failureHandler() default "";

    /**
     * Specifies the target endpoint.
     */
    String endpoint() default "";

    /**
     * Specifies the remote application name.
     */
    String remoteApplication() default "";

    /**
     * Specifies the remote module name.
     */
    String remoteModule() default "";

    /**
     * Specifies the remote platform name.
     */
    String remotePlatform() default "";

    /**
     * Specifies the named client configuration.
     */
    String clientConfig() default "";

    /**
     * Specifies the associated module name.
     */
    String module() default "";

    /**
     * Specifies the annotation style extension name.
     */
    String annotationStyle() default "";

    /**
     * Specifies the call timeout in milliseconds.
     */
    int timeoutMillis() default -1;

    /**
     * Specifies the service discovery timeout in milliseconds.
     */
    int serviceDiscoveryTimeoutMillis() default -1;

    /**
     * Specifies the retry count.
     */
    int retries() default -1;

    /**
     * Specifies the initial retry backoff in milliseconds.
     */
    int retryBackoffMillis() default -1;

    /**
     * Specifies the maximum retry backoff in milliseconds.
     */
    int retryMaxBackoffMillis() default -1;

    /**
     * Specifies the retry jitter in milliseconds.
     */
    int retryJitterMillis() default -1;

    /**
     * Specifies the serialization threshold used for I/O serialization decisions.
     */
    long serializationThreshold() default -1;

    /**
     * Specifies the deserialization threshold used for I/O deserialization decisions.
     */
    long deserializationThreshold() default -1;
}

