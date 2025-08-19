package io.effi.rpc.util;

/**
 * Provides common class operations.
 */
public final class ClassUtil {


    /**
     * Gets the class loader for the specified class.
     */
    public static ClassLoader findClassLoader(Class<?> c) {
        ClassLoader cl = null;
        if (c != null) {
            cl = c.getClassLoader();
        }
        if (cl == null) {
            try {
                cl = Thread.currentThread().getContextClassLoader();
            } catch (Throwable ignored) {
                // Cannot access thread context ClassLoader - fallback below
            }
            if (cl == null) {
                // fallback to this util class's loader
                cl = ClassUtil.class.getClassLoader();
                if (cl == null) {
                    try {
                        // fallback to system loader
                        cl = ClassLoader.getSystemClassLoader();
                    } catch (Throwable ignored) {
                        // Cannot access system ClassLoader - will return null
                    }
                }
            }
        }
        return cl;
    }

    private ClassUtil() {
    }

}
