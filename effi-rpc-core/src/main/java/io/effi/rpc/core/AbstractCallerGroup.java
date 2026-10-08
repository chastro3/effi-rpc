package io.effi.rpc.core;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallerGroup;
import io.effi.rpc.context.RpcType;
import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.parameter.MethodBinder;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.proxy.ProxyFactory;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeCapture;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.effi.rpc.component.serialization.options.SerializationOptions.SERIALIZER;
import static io.effi.rpc.context.options.CallerOptions.ENDPOINT;
import static io.effi.rpc.context.options.CallerOptions.PROXY;
import static io.effi.rpc.context.options.CallerOptions.TIMEOUT;
import static io.effi.rpc.context.options.GovernanceOptions.LOCATOR;

/**
 * Provides the common proxy and caller registry behavior for caller groups.
 */
public abstract class AbstractCallerGroup<T> extends AbstractPeerGroup<Caller<?>, T>
        implements CallerGroup<T>, InvocationHandler {

    private final Map<Method, MethodCaller> methodCallers = new LinkedHashMap<>();

    private final String proxyName;

    private final T proxy;

    protected AbstractCallerGroup(Builder<?, T, ?> builder) {
        super(builder);
        this.proxyName = builder.proxyName;
        this.proxy = createProxy(builder.module);
    }

    private T createProxy(ScopedModule module) {
        ProxyFactory proxyFactory = module.platform().preferredExtension(ProxyFactory.class, proxyName);
        return proxyFactory.createProxy(targetType, this);
    }

    @Override
    public final T proxy() {
        return proxy;
    }

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args, InvocationHandler.SuperInvoker superInvoker) {
        MethodCaller methodCaller = methodCallers.get(method);
        if (methodCaller == null) {
            throw new IllegalStateException("No RPC mapping configured for method: " + method.toGenericString());
        }
        Invocation invocation = methodCaller.binder().bind(args);
        return invokeCaller(methodCaller.caller(), methodCaller.rpcType(), invocation);
    }

    static Object invokeCaller(Caller<?> caller, RpcType rpcType, Invocation invocation) {
        return switch (rpcType) {
            case SYNC -> caller.blockingCall(invocation);
            case ASYNC -> caller.call(invocation).toCompletableFuture();
        };
    }

    public final String proxyName() {
        return proxyName;
    }

    protected final void registerMethodCaller(Method method, Caller<?> caller, RpcType rpcType, MethodBinding binding) {
        AssertUtil.notNull(method, "method");
        AssertUtil.notNull(caller, "caller");
        AssertUtil.notNull(rpcType, "rpcType");
        AssertUtil.notNull(binding, "binding");
        MethodCaller previous = methodCallers.putIfAbsent(method, new MethodCaller(caller, rpcType, new MethodBinder(binding)));
        AssertUtil.valid(previous == null, "Duplicate caller mapping for method: {}", method.toGenericString());
        register(caller);
    }

    private record MethodCaller(
            /** Caller bound to the method. */
            Caller<?> caller,
            /** RPC interaction type. */
            RpcType rpcType,
            /** Argument binder for the method. */
            MethodBinder binder
    ) {
    }

    /**
     * Assembles a caller group with shared proxy configuration.
     */
    public abstract static class Builder<G extends AbstractCallerGroup<T>, T, SELF extends Builder<G, T, SELF>>
            extends AbstractPeerGroup.Builder<G, Caller<?>, T, SELF> {

        protected String proxyName;

        protected String protocolName;

        protected Builder() {
        }

        public SELF proxy(String proxyName) {
            this.proxyName = proxyName;
            return self();
        }

        public SELF protocol(String protocolName) {
            this.protocolName = AssertUtil.notBlank(protocolName, "protocol");
            return self();
        }

        public SELF endpoint(String target) {
            addOption(ENDPOINT, target);
            return self();
        }

        public SELF locator(String locator) {
            addOption(LOCATOR, locator);
            return self();
        }

        public SELF serializer(String serializer) {
            addOption(SERIALIZER, serializer);
            return self();
        }

        public SELF timeout(int timeout) {
            addOption(TIMEOUT, timeout);
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

        // todo 这一块 内部不是很优雅
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
