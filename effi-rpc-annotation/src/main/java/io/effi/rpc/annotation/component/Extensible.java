package io.effi.rpc.annotation.component;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope;

/**
 * Marks a type as an SPI interface for extension loading.
 *
 * @see io.effi.rpc.component.extension.ExtensionLoader
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@ScopedComponent(kind = Kind.MULTI)
public @interface Extensible {

    /**
     * Specifies the primary implementation class name.
     */
    String value() default "";

    /**
     * Specifies the scope of the extension.
     */
    Scope scope() default Scope.PLATFORM;

    /**
     * Indicates if lazy loading is enabled for the extension.
     */
    boolean lazyLoad() default true;

}


