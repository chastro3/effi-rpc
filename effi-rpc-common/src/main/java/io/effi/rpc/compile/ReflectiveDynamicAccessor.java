package io.effi.rpc.compile;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class ReflectiveDynamicAccessor extends DynamicAccessor {

    private final Method[] methods;

    private final boolean[] staticMethods;

    private ReflectiveDynamicAccessor(
            Class<?> type, String[] methodNames,
            Class<?>[][] parameterTypes,
            Method[] methods,
            boolean[] staticMethods
    ) {
        super(type, methodNames, parameterTypes);
        this.methods = methods;
        this.staticMethods = staticMethods;
    }

    static DynamicAccessor create(Class<?> type) {
        Method[] methods = DynamicAccessorGenerator.publicMethods(type);
        String[] methodNames = new String[methods.length];
        Class<?>[][] parameterTypes = new Class<?>[methods.length][];
        boolean[] staticMethods = new boolean[methods.length];
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            methodNames[i] = method.getName();
            parameterTypes[i] = method.getParameterTypes();
            staticMethods[i] = Modifier.isStatic(method.getModifiers());
            trySetAccessible(method);
        }
        return new ReflectiveDynamicAccessor(type, methodNames, parameterTypes, methods, staticMethods);
    }

    @Override
    public Object invoke(Object target, int index, Object... args) {
        validateInvocation(index, args);
        Method method = methods[index];
        Object[] invocationArgs = args == null ? EMPTY_ARGS : args;
        try {
            return method.invoke(staticMethods[index] ? null : target, invocationArgs);
        } catch (InvocationTargetException e) {
            throw DynamicAccessorSupport.rethrow(e.getCause());
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IllegalStateException("Failed to invoke method: " + methodNames[index], e);
        }
    }

    private static void trySetAccessible(Method method) {
        try {
            method.trySetAccessible();
        } catch (RuntimeException ignored) {
        }
    }
}
