package io.effi.rpc.annotation.component;

import io.effi.rpc.constant.ResourcePaths;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks components with a defined scope and kind.
 *
 * <p>Classes annotated with this will have their metadata
 * automatically generated at compile time into
 * {@link ResourcePaths#COMPONENT_DESCRIPTOR_FILE}.
 *
 * <p>This properties file is used at runtime to
 * discover and manage these components according to their scope and kind.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface ScopedComponent {

    /**
     * Specifies the scope where the component is effective.
     */
    Scope scope() default Scope.UNIVERSAL;

    /**
     * Specifies whether the component allows single or multiple instances per scope.
     */
    Kind kind() default Kind.MULTI;

    /**
     * Defines available component scopes.
     */
    enum Scope {

        /**
         * Restricts the component to platform-level management;
         * allows access to the platform instance only.
         */
        PLATFORM,

        /**
         * Restricts the component to application-level management;
         * allows access to application and platform instances.
         */
        APPLICATION,

        /**
         * Restricts the component to module-level management;
         * allows access to module, application, and platform instances.
         */
        MODULE,

        /**
         * Enables the component to be managed at platform, application, or module level;
         * allows contextual access to all instances.
         */
        UNIVERSAL
    }

    /**
     * Defines allowed component kinds.
     */
    enum Kind {

        /**
         * Manages the component as a singleton within its scope.
         */
        SINGLE,

        /**
         * Manages the component as multiple instances identified by names.
         */
        MULTI
    }

}

