package io.effi.rpc.annotation.component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type as an SPI interface for extension loading.
 *
 * @see ExtensionLoader
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ScopedComponent
public @interface Extensible {

    /**
     * Specifies the default implementation class name.
     */
    String value() default "";

    /**
     * Defines the extension key for selecting an extension.
     */
    String key() default "";

    /**
     * Specifies the scope of the extension.
     */
    ScopedComponent.Scope scope() default ScopedComponent.Scope.PLATFORM;

    /**
     * Indicates if lazy loading is enabled for the extension.
     */
    boolean lazyLoad() default true;

}


