package io.effi.rpc.compile;

import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.nativetools.NativeUtil;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.ReflectionUtil;
import org.jspecify.annotations.NonNull;

final class DynamicAccessorFactory {

    private static final Logger logger = LoggerFactory.getLogger(DynamicAccessorFactory.class);

    private static final ClassValue<DynamicAccessor> ACCESSORS = new ClassValue<>() {

        @Override
        protected DynamicAccessor computeValue(@NonNull Class<?> type) {
            return create(type);
        }
    };

    private DynamicAccessorFactory() {
    }

    static DynamicAccessor fetch(Class<?> type) {
        return ACCESSORS.get(AssertUtil.notNull(type, "type"));
    }

    private static DynamicAccessor create(Class<?> type) {
        RuntimeClassLoader classLoader = RuntimeClassLoader.get(type);
        String accessorName = type.getName() + DynamicAccessor.SUFFIX;
        Class<?> existing = loadExistingAccessor(type, accessorName);
        if (existing != null) {
            return (DynamicAccessor) ReflectionUtil.newInstance(existing);
        }
        if (NativeUtil.inNativeImage()) {
            logger.debug("Native image detected; using fallback accessor for {}", type.getName());
            return createFallback(type, null);
        }
        try {
            String generatedName = runtimeAccessorName(type, accessorName);
            GeneratedInfo generatedInfo = DynamicAccessorGenerator.from(type, generatedName);
            Class<?> generated = classLoader.define(type, generatedInfo.qualifiedName(), generatedInfo.data());
            return (DynamicAccessor) ReflectionUtil.newInstance(generated);
        } catch (Throwable failure) {
            DynamicAccessorSupport.rethrowIfFatal(failure);
            return createFallback(type, failure);
        }
    }

    private static Class<?> loadExistingAccessor(Class<?> type, String accessorName) {
        ClassLoader targetLoader = type.getClassLoader();
        if (targetLoader == null) {
            return null;
        }
        try {
            Class<?> accessor = Class.forName(accessorName, false, targetLoader);
            // Parent-first loading may return an accessor generated for a same-named type in another loader.
            return accessor.getClassLoader() == targetLoader ? accessor : null;
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }

    private static String runtimeAccessorName(Class<?> type, String accessorName) {
        String pkg = type.getPackageName();
        String binaryName = pkg.isEmpty() ? accessorName : accessorName.substring(pkg.length() + 1);
        // Include target identity so same-named types from different loaders do not share a generated class.
        return binaryName + "$" + Integer.toHexString(System.identityHashCode(type));
    }

    private static DynamicAccessor createFallback(Class<?> type, Throwable cause) {
        DynamicAccessor accessor = MethodHandleDynamicAccessor.create(type);
        if (accessor == null) {
            accessor = ReflectiveDynamicAccessor.create(type);
        }
        if (cause != null) {
            logger.warn("Failed to generate dynamic accessor for {}; using {}", cause, type.getName(), ObjectUtil.simpleClassName(accessor));
        }
        return accessor;
    }
}
