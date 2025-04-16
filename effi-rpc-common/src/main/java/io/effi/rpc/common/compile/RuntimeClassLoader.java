package io.effi.rpc.common.compile;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class RuntimeClassLoader extends ClassLoader {

    private static final Map<ClassLoader, WeakReference<RuntimeClassLoader>> LOADERS = new WeakHashMap<>();

    private static final ClassLoader SELF_CLASS_LOADER = getClassLoader(RuntimeClassLoader.class);

    private static final AtomicReference<RuntimeClassLoader> SELF = new AtomicReference<>();

    private static volatile Method DEFINE_CLASS_METHOD;

    private RuntimeClassLoader(ClassLoader parent) {
        super(parent);
    }

    public static RuntimeClassLoader get(Class<?> type) {
        ClassLoader cl = getClassLoader(type);
        // fast‑path: same parent as this class
        if (SELF_CLASS_LOADER.equals(cl)) {
            return SELF.updateAndGet(existing -> existing != null ? existing : new RuntimeClassLoader(cl));
        }
        // 2. normal search:
        synchronized (LOADERS) {
            WeakReference<RuntimeClassLoader> ref = LOADERS.get(cl);
            if (ref != null) {
                RuntimeClassLoader runtimeClassLoader = ref.get();
                if (runtimeClassLoader != null)
                    return runtimeClassLoader;
                else
                    LOADERS.remove(cl); // the value has been GC-reclaimed, but still not the key (defensive sanity)
            }
            RuntimeClassLoader runtimeClassLoader = new RuntimeClassLoader(cl);
            LOADERS.put(cl, new WeakReference<>(runtimeClassLoader));
            return runtimeClassLoader;
        }
    }

    public static void remove(ClassLoader parent) {
        if (SELF_CLASS_LOADER.equals(parent)) {
            SELF.set(null);
        } else {
            synchronized (LOADERS) {
                LOADERS.remove(parent);
            }
        }
    }

    public Class<?> define(String name, byte[] bytes) {
        ProtectionDomain pd = getClass().getProtectionDomain();
        try {
            // First, try defining in parent to get package/protected access
            return (Class<?>) getDefineClassMethod().invoke(getParent(), name, bytes, 0, bytes.length, pd);
        } catch (Exception ignored) {
            // Fallback: define in this loader
        }
        return defineClass(name, bytes, 0, bytes.length, pd);
    }

    private static ClassLoader getClassLoader(Class<?> type) {
        ClassLoader parent = type.getClassLoader();
        return parent != null ? parent : ClassLoader.getSystemClassLoader();
    }

    private static Method getDefineClassMethod() throws Exception {
        if (DEFINE_CLASS_METHOD == null) {
            synchronized (LOADERS) {
                if (DEFINE_CLASS_METHOD == null) {
                    DEFINE_CLASS_METHOD = ClassLoader.class.getDeclaredMethod("defineClass",
                            String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
                    try {
                        DEFINE_CLASS_METHOD.setAccessible(true);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return DEFINE_CLASS_METHOD;
    }

}
