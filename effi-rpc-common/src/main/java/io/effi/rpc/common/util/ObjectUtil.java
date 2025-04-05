package io.effi.rpc.common.util;

import java.util.Objects;

/**
 * Utility class for common object operations.
 */
public final class ObjectUtil {

    /**
     * Get the simple class name of the object.
     * <p>if the object is null, return "null object",
     * otherwise return the abbreviation of the object class name.</p>
     *
     * @param o
     * @return
     */
    public static String simpleClassName(Object o) {
        if (o == null) {
            return "null_object";
        } else {
            return simpleClassName(o.getClass());
        }
    }

    /**
     * Get the short name of the class.
     *
     * @param type
     * @return
     */
    public static String simpleClassName(Class<?> type) {
        Objects.requireNonNull(type);
        return type.getSimpleName();
    }

    /**
     * Get the default value if the object is null.
     *
     * @param o
     * @param defaultValue
     * @param <T>
     * @return
     */
    public static <T> T isNull(T o, T defaultValue) {
        return o == null ? defaultValue : o;
    }

    /**
     * Get the lowercase name of the class.
     *
     * @param type
     * @return
     */
    public static String lowercaseName(Class<?> type) {
        String simpleName = simpleClassName(type);
        char[] chars = simpleName.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    public static String objectToString(Object o) {
        if (o == null) {
            return "null";
        } else {
            return o.getClass().getName() + "@" + Integer.toHexString(o.hashCode());
        }
    }

}
