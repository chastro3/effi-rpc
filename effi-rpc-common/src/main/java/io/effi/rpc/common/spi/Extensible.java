package io.effi.rpc.common.spi;

import java.lang.annotation.*;

/**
 * Marks an interface as a Service Provider Interface (SPI) for extension loading.
 * Apply to interfaces with multiple implementations managed by an extension loader.
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
     * Defines the extension key for selecting an extension during loading.
     */
    String key() default "";

    /**
     * Indicates whether lazy loading is enabled for the extension.
     */
    boolean lazyLoad() default true;

}

