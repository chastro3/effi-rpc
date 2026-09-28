package io.effi.rpc.boot;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallerGroup;
import io.effi.rpc.context.RpcType;
import io.effi.rpc.context.annotation.AnnotationParameterWrapper;
import io.effi.rpc.context.parameter.ParameterLinking;
import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.proxy.ProxyFactory;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeCapture;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static io.effi.rpc.context.options.CallerOptions.PROXY;

/**
 * Provides the common proxy and caller registry behavior for caller groups.
 */
public abstract class AbstractCallerGroup<T> extends AbstractPeerGroup<Caller<?>, T>
        implements CallerGroup<T>, InvocationHandler {

    private final Map<Method, MethodCaller> methodCallers = new LinkedHashMap<>();

    private final String proxyName;

    protected AbstractCallerGroup(Builder<?, T, ?> builder) {
        super(builder);
        this.proxyName = builder.proxyName;
        onInitialized(targetType, createProxy(builder.module));
    }

    @Override
    public final String proxy() {
        return proxyName;
    }

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args, Callable<?> superInvoker) {
        MethodCaller methodCaller = methodCallers.get(method);
        if (methodCaller == null) {
            throw new IllegalStateException("No RPC mapping configured for method: " + method.toGenericString());
        }
        Object[] arguments = wrapArgs(methodCaller.linkings(), args, methodCaller.caller());
        return invokeCaller(methodCaller.caller(), methodCaller.rpcType(), arguments);
    }

    @Override
    public <R> R invoke(Caller<?> caller, Object... args) {
        return null;
    }

    protected final void registerMethodCaller(Method method, Caller<?> caller, RpcType rpcType, ParameterLinking[] linkings) {
        AssertUtil.notNull(method, "method");
        AssertUtil.notNull(caller, "caller");
        AssertUtil.notNull(rpcType, "rpcType");
        AssertUtil.notNull(linkings, "linkings");
        MethodCaller previous = methodCallers.putIfAbsent(method, new MethodCaller(caller, rpcType, linkings));
        AssertUtil.valid(previous == null, "Duplicate caller mapping for method: {}", method.toGenericString());
        register(caller);
    }

    private T createProxy(ScopedModule module) {
        ProxyFactory proxyFactory = module.platform().preferredExtension(ProxyFactory.class, proxyName);
        return proxyFactory.createProxy(targetType, this);
    }

    static Object invokeCaller(Caller<?> caller, RpcType rpcType, Object[] args) {
        return switch (rpcType) {
            case SYNC -> caller.blockingCall(args);
            case ASYNC -> caller.call(args).toCompletableFuture();
        };
    }

    static Object[] wrapArgs(ParameterLinking[] linkings, Object[] args, Caller<?> caller) {
        if (CollectionUtil.isEmpty(args)) {
            return new Object[0];
        }
        Object[] result = new Object[linkings.length];
        for (int i = 0; i < linkings.length; i++) {
            ParameterLinking linking = linkings[i];
            AnnotationParameterWrapper<?> wrapper = linking.wrapper();
            result[i] = wrapper == null
                    ? args[i]
                    : wrapper.wrap(args[i], linking.parameter(), caller);
        }
        return result;
    }

    private record MethodCaller(Caller<?> caller, RpcType rpcType, ParameterLinking[] linkings) {
    }

    /**
     * Assembles a caller group with shared proxy configuration.
     */
    public abstract static class Builder<G extends AbstractCallerGroup<T>, T, SELF extends Builder<G, T, SELF>>
            extends AbstractPeerGroup.Builder<G, Caller<?>, T, SELF> {

        protected String proxyName;

        protected Builder() {
        }

        public SELF proxy(String proxyName) {
            this.proxyName = proxyName;
            return self();
        }

        @Override
        protected void validate() {
            super.validate();
            AssertUtil.notNull(targetType, "targetType");
            AssertUtil.valid(targetType.isInterface(), "targetType must be an interface");
        }

        protected final void resolveProxy() {
            if (StringUtil.isBlank(proxyName)) {
                proxyName = options.option(PROXY);
            }
        }

        protected final ReturnType returnType(Method method) {
            Type type = method.getGenericReturnType();
            if (type instanceof ParameterizedType parameterizedType
                    && parameterizedType.getRawType() == CompletableFuture.class) {
                Type actualType = parameterizedType.getActualTypeArguments()[0];
                return new ReturnType(TypeCapture.of(actualType), RpcType.ASYNC);
            }
            return new ReturnType(TypeCapture.of(type), RpcType.SYNC);
        }

        protected record ReturnType(TypeCapture<?> typeCapture, RpcType rpcType) {
        }
    }
}
