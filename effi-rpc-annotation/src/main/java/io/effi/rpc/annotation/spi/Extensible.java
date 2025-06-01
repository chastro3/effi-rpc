package io.effi.rpc.annotation.spi;

import java.lang.annotation.*;

/**
 * Marks a type as an SPI interface for extension loading.
 *
 * @see ExtensionLoader
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
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
     * Indicates if lazy loading is enabled for the extension.
     */
    boolean lazyLoad() default true;

}


