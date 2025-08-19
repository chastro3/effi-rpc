package io.effi.rpc.nativetools;

import io.effi.rpc.util.CollectionUtil;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a native configuration item.
 */
public interface NativeConfig<T> {

    /**
     * Returns the name of the native configuration item.
     */
    String name();

    /**
     * Converts this native configuration item to its JSON representation.
     */
    T toJsonConfig();

    /**
     * Checks if the configuration item has a resource.
     */
    boolean hasResource();

    /**
     * Represents a configuration entry in native config.
     */
    interface Item {
        /**
         * Converts the current configuration entry to a map representation.
         */
        Map<String, Object> toMap();

        /**
         * Converts a list of configuration entries to a list of maps.
         */
        static <T extends Item> List<Map<String, Object>> toMapList(List<T> list) {
            return CollectionUtil.isEmpty(list) ? Collections.emptyList()
                    : list.stream().map(Item::toMap).toList();
        }
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    @Documented
    @interface Reflect {

        Class<?> typeReached() default Void.class;

        String typeReachedName() default "";

        boolean allDeclaredClasses() default false;

        boolean allDeclaredMethods() default false;

        boolean allDeclaredFields() default false;

        boolean allDeclaredConstructors() default false;

        boolean allPublicClasses() default false;

        boolean allPublicMethods() default false;

        boolean allPublicFields() default false;

        boolean allPublicConstructors() default false;

        boolean allRecordComponents() default false;

        boolean allPermittedSubclasses() default false;

        boolean allNestMembers() default false;

        boolean allSigners() default false;

        boolean queryAllDeclaredMethods() default false;

        boolean queryAllDeclaredConstructors() default false;

        boolean queryAllPublicMethods() default false;

        boolean queryAllPublicConstructors() default false;

        boolean unsafeAllocated() default false;

    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    @Documented
    @interface Proxy {

        Class<?> typeReached() default Void.class;

        String typeReachedName() default "";

        String interfaceName() default "";
    }
}

