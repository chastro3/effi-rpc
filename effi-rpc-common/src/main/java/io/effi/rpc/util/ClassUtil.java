package io.effi.rpc.util;

/**
 * Provides common class operations.
 */
public final class ClassUtil {


    /**
     * Get the class loader for the specified class.
     */
    public static ClassLoader getClassLoader(Class<?> c) {
        ClassLoader cl = null;
        if (!c.getName().startsWith("org.apache.dubbo")) {
            cl = c.getClassLoader();
        }
        if (cl == null) {
            try {
                cl = Thread.currentThread().getContextClassLoader();
            } catch (Exception ignored) {
                // Cannot access thread context ClassLoader - falling back to system class loader...
            }
            if (cl == null) {
                // No thread context class loader -> use class loader of this class.
                cl = c.getClassLoader();
                if (cl == null) {
                    // getClassLoader() returning null indicates the bootstrap ClassLoader
                    try {
                        cl = ClassLoader.getSystemClassLoader();
                    } catch (Exception ignored) {
                        // Cannot access system ClassLoader - oh well, maybe the caller can live with null...
                    }
                }
            }
        }
        return cl;
    }
}
