package io.effi.rpc.util;

import java.util.Objects;

/**
 * Provides common object operations.
 */
public final class ObjectUtil {

    /**
     * Gets the simple class name of the object.
     */
    public static String simpleClassName(Object o) {
        if (o == null) {
            return "null_object";
        } else {
            return simpleClassName(o.getClass());
        }
    }

    /**
     * Gets the simple class name of the class.
     */
    public static String simpleClassName(Class<?> type) {
        Objects.requireNonNull(type);
        return type.getSimpleName();
    }


    /**
     * Gets the lowercase name of the class.
     */
    public static String lowercaseName(Class<?> type) {
        String simpleName = simpleClassName(type);
        char[] chars = simpleName.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

}
