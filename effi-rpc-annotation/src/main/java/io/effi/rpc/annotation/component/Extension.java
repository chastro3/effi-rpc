package io.effi.rpc.annotation.component;

import io.effi.rpc.util.Ordered;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as an implementation of an SPI interface.
 *
 * @see io.effi.rpc.component.extension.ExtensionLoader
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Extension {

    /**
     * Specifies the name(s) of the implementation.
     */
    String[] value();

    /**
     * Specifies the interfaces this class implements.
     * Defaults to all interfaces marked with {@link Extensible}.
     */
    Class<?>[] interfaces() default {};

    /**
     * Defines conditions for enabling the extension.
     */
    String[] onClass() default {};

    boolean primary() default false;

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

    /**
     * Specifies the tags of the extension.
     */
    String[] tags() default {};

    /**
     * Defines the scope of an SPI extension.
     */
    enum Scope {

        /**
         * Single shared instance across the application.
         */
        SINGLETON,

        /**
         * New instance for every usage.
         */
        PROTOTYPE
    }
}


