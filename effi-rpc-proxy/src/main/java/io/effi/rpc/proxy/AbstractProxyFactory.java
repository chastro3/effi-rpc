package io.effi.rpc.proxy;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import static io.effi.rpc.util.ReflectionUtil.invokeObjectMethod;

/**
 * Provides the shared proxy creation flow for proxy dialects.
 * <p>
 * Centralizes Object method handling, interface default method dispatch, and target-backed
 * invocation before delegating to {@link InvocationHandler}.
 */
public abstract class AbstractProxyFactory implements ProxyFactory {

    private static final Object[] NO_ARGS = new Object[0];

    private static final InvocationHandler.SuperInvoker NO_TARGET_INVOKER = () -> {
        throw new UnsupportedOperationException("Interface proxy has no original implementation");
    };

    @Override
    public <T> T createProxy(Class<T> interfaceClass, InvocationHandler handler) {
        if (!interfaceClass.isInterface()) {
            throw new IllegalArgumentException("Proxy type must be an interface: " + interfaceClass.getName());
        }
        try {
            return doCreateProxy(interfaceClass, interfaceInvoker(handler));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create proxy for " + interfaceClass.getName(), e);
        }
    }

    @Override
    public <T> T createProxy(T target, InvocationHandler handler) {
        Objects.requireNonNull(target, "target");
        try {
            return doCreateProxy(target, targetInvoker(target, handler));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create proxy for " + target.getClass().getName(), e);
        }
    }

    protected abstract <T> T doCreateProxy(T target, ProxyMethodInvoker invoker) throws Exception;

    private ProxyMethodInvoker targetInvoker(Object target, InvocationHandler handler) {
        return (proxy, method, args) -> {
            if (Object.class.equals(method.getDeclaringClass())) {
                return invokeObjectMethod(target, method, args);
            }
            return handler.invoke(proxy, method, args, () -> invokeTargetMethod(target, method, args));
        };
    }

    private Object invokeTargetMethod(Object target, Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    protected abstract <T> T doCreateProxy(Class<T> interfaceClass, ProxyMethodInvoker invoker) throws Exception;

    private ProxyMethodInvoker interfaceInvoker(InvocationHandler handler) {
        return (proxy, method, args) -> {
            if (method.isDefault()) {
                return invokeDefaultMethod(proxy, method, args);
            }
            if (Object.class.equals(method.getDeclaringClass())) {
                // Interface proxies have no target, so Object methods follow proxy identity.
                return invokeProxyObjectMethod(proxy, method, args);
            }
            return handler.invoke(proxy, method, args, NO_TARGET_INVOKER);
        };
    }

    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args) throws Throwable {
        Class<?> declaringClass = method.getDeclaringClass();
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup());
        return lookup.unreflectSpecial(method, declaringClass)
                .bindTo(proxy)
                .invokeWithArguments(args == null ? NO_ARGS : args);
    }

    private Object invokeProxyObjectMethod(Object proxy, Method method, Object[] args) {
        return switch (method.getName()) {
            case "getClass" -> proxy.getClass();
            case "hashCode" -> System.identityHashCode(proxy);
            case "toString" -> proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
            case "equals" -> proxy == args[0];
            default -> throw new UnsupportedOperationException("Unsupported Object method: " + method);
        };
    }
}
