package io.effi.rpc.common.spi;

import io.effi.rpc.common.util.Ordered;

import java.lang.annotation.*;

/**
 * Marks a class as an implementation for an SPI interface,
 * making it available for SPI-based selection.
 *
 * @see ExtensionLoader
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Extension {

    /**
     * Specifies the name(s) of the current implementation.
     */
    String[] value() default {};

    /**
     * Specifies the interfaces that this class implements.
     * Defaults to all interfaces marked with {@link Extensible}.
     */
    Class<?>[] interfaces() default {};

    /**
     * Defines conditions for enabling this extension.
     */
    String[] onClass() default {};

    /**
     * Indicates if this extension should override others with the same name.
     */
    boolean override() default false;

    /**
     * Specifies the extension scope.
     */
    Scope scope() default Scope.SINGLETON;

    /**
     * Specifies the order of the extension.
     */
    int order() default Ordered.DEFAULT;
}

