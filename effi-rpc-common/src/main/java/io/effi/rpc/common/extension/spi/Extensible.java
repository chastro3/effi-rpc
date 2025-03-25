package io.effi.rpc.common.extension.spi;

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
     *
     * @return the default implementation class name
     */
    String value() default "";

    /**
     * Defines the extension key for selecting an extension during loading.
     *
     * @return the extension key
     */
    String key() default "";

    /**
     * Indicates whether lazy loading is enabled for the extension.
     *
     * @return true if lazy loading is enabled, false otherwise
     */
    boolean lazyLoad() default true;

}

