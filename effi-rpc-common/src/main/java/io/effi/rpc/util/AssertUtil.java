package io.effi.rpc.util;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import static io.effi.rpc.util.StringUtil.format;

/**
 * Provides assertion operations.
 */
public final class AssertUtil {

    private AssertUtil() {
    }

    /**
     * Checks if the annotation is present.
     *
     * @param type           annotated type
     * @param annotationType required annotation type
     * @return the required annotation instance
     * @throws IllegalArgumentException if the annotation is absent
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
     *
     * @param condition condition to validate
     * @param message   message template
     * @param args      message arguments
     */
    public static void valid(boolean condition, String message, Object... args) {
        valid(condition, format(message, args));
    }

    /**
     * Checks if the condition is true with a custom message.
     *
     * @param condition condition to validate
     * @param message   failure message
     */
    public static void valid(boolean condition, String message) {
        valid(condition, IllegalArgumentException::new, message);
    }

    /**
     * Checks if the condition is true with a custom exception factory.
     *
     * @param condition condition to validate
     * @param exception exception factory
     * @param message   failure message
     * @param <E>       exception type
     */
    public static <E extends RuntimeException> void valid(boolean condition, Function<String, E> exception, String message) {
        if (!condition) {
            throw exception.apply(message);
        }
    }

    /**
     * Checks if the condition is true with a custom exception factory.
     *
     * @param condition condition to validate
     * @param exception exception factory
     * @param message   message template
     * @param args      message arguments
     * @param <E>       exception type
     */
    public static <E extends RuntimeException> void valid(boolean condition, Function<String, E> exception, String message, Object... args) {
        valid(condition, exception, format(message, args));
    }

    /**
     * Checks that the object is not null with a name in the error message.
     *
     * @param object object to validate
     * @param name   parameter name
     * @return the validated object
     */
    public static <T> T notNull(T object, String name) {
        if (object == null) {
            throw new IllegalArgumentException("Parameter '" + name + "' cannot be null.");
        }
        return object;
    }

    /**
     * Checks that the string is not blank.
     *
     * @param str  string to validate
     * @param name parameter name
     * @return the validated string
     */
    public static String notBlank(String str, String name) {
        if (StringUtil.isBlank(str)) {
            throw new IllegalArgumentException("Parameter '" + name + "' cannot be blank.");
        }
        return str;
    }
}

