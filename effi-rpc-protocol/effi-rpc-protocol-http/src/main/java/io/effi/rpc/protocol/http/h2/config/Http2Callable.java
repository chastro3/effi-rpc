package io.effi.rpc.protocol.http.h2.config;

import java.lang.annotation.*;

/**
 * Support be h2 protocol call.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Http2Callable {

    /**
     * The path of the HTTP request.
     */
    String path() default "";

    /**
     * The headers of the HTTP request.
     */
    String[] headers() default {};

    /**
     * The content type of the HTTP.
     */
    String contentType() default "application/json";

    /**
     * Whether to use SSL.
     */
    boolean ssl() default true;
}
