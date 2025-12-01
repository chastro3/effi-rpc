package io.effi.rpc.util;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import static io.effi.rpc.util.StringUtil.format;

/**
 * Provides assertion operations.
 */
public final class AssertUtil {

    /**
     * Checks if the annotation is present.
     */
    public static <T extends Annotation> T requireAnnotation(Class<?> type, Class<T> annotationType) {
        T annotation = type.getAnnotation(annotationType);
        if (annotation == null) {
            throw new IllegalArgumentException("Missing required annotation @"
                    + annotationType.getSimpleName() + "on " + type.getName());
        }
        return annotation;
    }

    /**
     * Checks if the condition is true with a custom message.
     */
    public static void valid(boolean condition, String message, Object... args) {
        valid(condition, format(message, args));
    }

    public static <E extends RuntimeException> void valid(boolean condition, Function<String, E> exception, String message, Object... args) {
        valid(condition, exception, format(message, args));
    }

    /**
     * Checks if the condition is true with a custom message.
     */
    public static void valid(boolean condition, String message) {
        valid(condition, IllegalArgumentException::new, message);
    }

    public static <E extends RuntimeException> void valid(boolean condition, Function<String, E> exception, String message) {
        if (!condition) {
            throw exception.apply(message);
        }
    }

    /**
     * Checks that the object is not null with a name in the error message.
     */
    public static <T> T notNull(T object, String name) {
        if (object == null) {
            throw new IllegalArgumentException("Parameter '" + name + "' cannot be null.");
        }
        return object;
    }

    /**
     * Checks that the string is not blank.
     */
    public static String notBlank(String str, String name) {
        if (StringUtil.isBlank(str)) {
            throw new IllegalArgumentException("Parameter '" + name + "' cannot be blank.");
        }
        return str;
    }

    private AssertUtil() {
    }
}

