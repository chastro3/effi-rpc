package io.effi.rpc.util;

import java.util.Objects;

/**
 * Provides assertion operations.
 */
public final class AssertUtil {

    private AssertUtil() {
    }

    /**
     * Checks if the condition is true.
     */
    public static void condition(boolean condition) {
        condition(condition, null);
    }

    /**
     * Checks if the condition is true with a custom message.
     */
    public static void condition(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks that the object is not null.
     */
    public static <T> T notNull(T object) {
        return Objects.requireNonNull(object);
    }

    /**
     * Checks that each object is not null.
     */
    public static void notNull(Object... objects) {
        for (Object object : objects) {
            notNull(object);
        }
    }

    /**
     * Checks that the object is not null with a name in the error message.
     */
    public static <T> T notNull(T object, String name) {
        if (object == null) {
            throw new IllegalArgumentException(Messages.notNull(StringUtil.isBlankOrDefault(name, "object")));
        }
        return object;
    }

    /**
     * Checks that the string is not blank.
     */
    public static String notBlank(String str, String name) {
        if (StringUtil.isBlank(str)) {
            throw new IllegalArgumentException(Messages.notBlank(StringUtil.isBlankOrDefault(name, "object")));
        }
        return str;
    }

    /**
     * Checks if two objects are equal.
     */
    public static void equals(Object expected, Object actual) {
        equals(expected, actual, null);
    }

    /**
     * Checks if two objects are equal with a custom message.
     */
    public static void equals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message);
        }
    }
}

