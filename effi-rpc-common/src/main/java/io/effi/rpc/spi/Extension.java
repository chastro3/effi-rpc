package io.effi.rpc.spi;

import io.effi.rpc.util.Ordered;

import java.lang.annotation.*;

/**
 * Marks a class as an implementation of an SPI interface.
 *
 * @see ExtensionLoader
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Extension {

    /**
     * Specifies the name(s) of the implementation.
     */
    String[] value() default {};

    /**
     * Specifies the interfaces this class implements.
     * Defaults to all interfaces marked with {@link Extensible}.
     */
    Class<?>[] interfaces() default {};

    /**
     * Defines conditions for enabling the extension.
     */
    String[] onClass() default {};

    /**
     * Indicates if this extension overrides others with the same name.
     */
    boolean override() default false;

    /**
     * Specifies the extension's scope.
     */
    Scope scope() default Scope.SINGLETON;

    /**
     * Specifies the order of the extension.
     */
    int order() default Ordered.DEFAULT;
}


