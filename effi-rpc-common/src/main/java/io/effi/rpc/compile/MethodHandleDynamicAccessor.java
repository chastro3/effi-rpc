package io.effi.rpc.compile;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

final class MethodHandleDynamicAccessor extends DynamicAccessor {

    private final MethodHandle[] handles;

    private final boolean[] staticMethods;

    private MethodHandleDynamicAccessor(
            Class<?> type,
            String[] methodNames,
            Class<?>[][] parameterTypes,
            MethodHandle[] handles,
            boolean[] staticMethods
    ) {
        super(type, methodNames, parameterTypes);
        this.handles = handles;
        this.staticMethods = staticMethods;
    }

    static DynamicAccessor create(Class<?> type) {
        Method[] methods = DynamicAccessorGenerator.publicMethods(type);
        String[] methodNames = new String[methods.length];
        Class<?>[][] parameterTypes = new Class<?>[methods.length][];
        MethodHandle[] handles = new MethodHandle[methods.length];
        boolean[] staticMethods = new boolean[methods.length];
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            MethodHandle handle = unReflect(method);
            if (handle == null) {
                return null;
            }
            methodNames[i] = method.getName();
            parameterTypes[i] = method.getParameterTypes();
            handles[i] = handle;
            staticMethods[i] = Modifier.isStatic(method.getModifiers());
        }
        return new MethodHandleDynamicAccessor(type, methodNames, parameterTypes, handles, staticMethods);
    }

    @Override
    public Object invoke(Object target, int index, Object... args) {
        validateInvocation(index, args);
        Object[] invocationArgs = args;
        if (!staticMethods[index]) {
            invocationArgs = new Object[(args == null ? 0 : args.length) + 1];
            invocationArgs[0] = target;
            if (args != null) {
                System.arraycopy(args, 0, invocationArgs, 1, args.length);
            }
        } else if (invocationArgs == null) {
            invocationArgs = EMPTY_ARGS;
        }
        try {
            return handles[index].invokeWithArguments(invocationArgs);
        } catch (Throwable throwable) {
            throw DynamicAccessorSupport.rethrow(throwable);
        }
    }

    private static MethodHandle unReflect(Method method) {
        try {
            Class<?> declaringType = method.getDeclaringClass();
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringType, MethodHandles.lookup());
            return lookup.unreflect(method);
        } catch (IllegalAccessException | IllegalArgumentException | SecurityException ignored) {
            try {
                return MethodHandles.publicLookup().unreflect(method);
            } catch (IllegalAccessException | RuntimeException ignoredAgain) {
                return null;
            }
        }
    }
}
