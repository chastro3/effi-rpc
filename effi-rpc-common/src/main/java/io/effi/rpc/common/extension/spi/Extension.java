package io.effi.rpc.common.extension.spi;

import io.effi.rpc.common.extension.Ordered;

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
     *
     * @return the name(s) of the implementation
     */
    String[] value() default {};

    /**
     * Specifies the interfaces that this class implements.
     * Defaults to all interfaces marked with {@link Extensible}.
     *
     * @return the interfaces implemented by this class
     */
    Class<?>[] interfaces() default {};

    /**
     * Defines conditions for enabling this extension.
     *
     * @return the enabling condition
     */
    String[] onClass() default {};

    /**
     * Indicates if this extension should override others with the same name.
     *
     * @return true if overriding, false otherwise
     */
    boolean override() default false;

    /**
     * Specifies the extension scope, defaulting to {@link Scope#SINGLETON}.
     *
     * @return the scope of the extension
     */
    Scope scope() default Scope.SINGLETON;

    /**
     * Specifies the order of the extension, defaulting to 0.
     *
     * @return the order of the extension
     */
    int order() default Ordered.DEFAULT;
}

