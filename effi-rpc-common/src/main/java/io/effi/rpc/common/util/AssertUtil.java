package io.effi.rpc.common.util;

import java.util.Objects;

/**
 * Utility class for assert operations.
 */
public final class AssertUtil {

    private AssertUtil() {
    }

    /**
     * Determine if a condition is valid, and throw a AssertionError exception when not.
     *
     * @param condition
     */
    public static void condition(boolean condition) {
        condition(condition, null);
    }

    /**
     * Determine if a condition is valid, and throw a AssertionError exception when not.
     *
     * @param condition
     * @param message
     */
    public static void condition(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Determine if an object is not null, and throw a NullPointerException exception when it is.
     *
     * @param object
     * @param <T>
     * @return
     */
    public static <T> T notNull(T object) {
        return Objects.requireNonNull(object);
    }

    /**
     * Determine if an object is not null, and throw a NullPointerException exception when it is.
     *
     * @param objects
     */
    public static void notNull(Object... objects) {
        for (Object object : objects) {
            notNull(object);
        }
    }

    /**
     * Determine if an object is not null, and throw a NullPointerException exception when it is.
     *
     * @param object
     * @param name
     * @param <T>
     * @return
     */
    public static <T> T notNull(T object, String name) {
        if (object == null) {
            String message = Messages.notNull(StringUtil.isBlankOrDefault(name, "object"));
            throw new IllegalArgumentException(message);
        }
        return object;
    }

    /**
     * Determine if a string is not empty.
     *
     * @param str
     * @param name
     */
    public static String notBlank(String str, String name) {
        if (StringUtil.isBlank(str)) {
            String message = Messages.notBlank(StringUtil.isBlankOrDefault(name, "object"));
            throw new IllegalArgumentException(message);
        }
        return str;
    }

    /**
     * Determine whether the two objects are equal.
     *
     * @param expected
     * @param actual
     */
    public static void equals(Object expected, Object actual) {
        equals(expected, actual, null);
    }

    /**
     * Determine whether the two objects are equal.
     *
     * @param expected
     * @param actual
     * @param message
     */
    public static void equals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message);
        }
    }

}
