package io.effi.rpc.util;

import io.effi.rpc.util.resoruce.Cleanable;
import io.effi.rpc.util.resoruce.Closeable;

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 * Provides common object operations.
 */
public final class ObjectUtil {

    /**
     * Returns the object name: {@code id()} if {@link Identifiable},
     * otherwise lowercase class name.
     */
    public static String resolveName(Object obj) {
        String name = null;
        if (obj instanceof Identifiable identifiable) {
            name = identifiable.id();
        }
        return StringUtil.isBlank(name)
                ? lowercaseName(obj.getClass())
                : name;
    }

    /**
     * Releases the object by calling {@code clear()} or {@code close()} if applicable.
     */
    public static void release(Object obj) {
        if (obj instanceof Cleanable cleanable) {
            cleanable.clear();
        }
        if (obj instanceof Closeable closeable && closeable.isActive()) {
            closeable.close();
        }
    }

    /**
     * Gets the annotation name.
     */
    public static String annotationName(Class<? extends Annotation> type) {
        return "@" + type.getSimpleName();
    }

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

    private ObjectUtil() {
    }

}
