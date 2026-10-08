package io.effi.rpc.compile;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.ObjectUtil;

import java.util.Arrays;

/**
 * Provides indexed invocation of public methods through generated or fallback accessors.
 * <p>
 * Accessor instances are immutable after construction and cached per target class.
 */
public abstract class DynamicAccessor {

    public static final String SUFFIX = "$" + ObjectUtil.simpleClassName(DynamicAccessor.class);

    public static final String INTERNAL_NAME = DynamicAccessor.class.getName().replace('.', '/');

    protected static final Object[] EMPTY_ARGS = new Object[0];

    protected final String[] methodNames;

    protected final Class<?>[][] parameterTypes;

    protected DynamicAccessor(Class<?> type, String[] methodNames, Class<?>[][] parameterTypes) {
        this.methodNames = methodNames;
        this.parameterTypes = parameterTypes;
    }

    /**
     * Returns the cached accessor for the specified class.
     *
     * @param type target class
     * @return cached dynamic accessor
     */
    public static DynamicAccessor fetch(Class<?> type) {
        return DynamicAccessorFactory.fetch(type);
    }

    /**
     * Invokes the indexed public method.
     *
     * @param target invocation target, ignored for static methods
     * @param index  method index
     * @param args   method arguments
     * @return invocation result, or {@code null} for void methods
     * @throws IllegalArgumentException if the index or arguments are invalid
     */
    public Object invoke(Object target, int index, Object... args) {
        throw new IllegalArgumentException("No methods found in " + this);
    }

    /**
     * Finds the index of a public method by name and parameter types.
     *
     * @param methodName method name
     * @param paramTypes method parameter types
     * @return method index
     * @throws IllegalArgumentException if the method is not found
     */
    public int findMethodIndex(String methodName, Class<?>... paramTypes) {
        if (CollectionUtil.isEmpty(methodNames)) {
            throw new IllegalStateException("No methods generated in this accessor");
        }
        if (CollectionUtil.isEmpty(paramTypes)) {
            paramTypes = null;
        }
        for (int i = 0, n = methodNames.length; i < n; i++) {
            Class<?>[] expected = parameterTypes[i];
            if (methodNames[i].equals(methodName)
                    && ((paramTypes == null && CollectionUtil.isEmpty(expected))
                    || Arrays.equals(paramTypes, expected))) {
                return i;
            }
        }
        throw new IllegalArgumentException(
                "Unable to find public method: " + methodName + " " + Arrays.toString(paramTypes)
        );
    }

    protected final void validateInvocation(int index, Object[] args) {
        DynamicAccessorSupport.validateInvocation(methodNames, parameterTypes, index, args);
    }
}
