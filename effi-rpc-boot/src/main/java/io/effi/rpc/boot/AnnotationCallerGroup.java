package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.CallerGroup;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.RpcType;
import io.effi.rpc.context.annotation.AnnotationParameterWrapper;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.annotation.AnnotationStyleResolver;
import io.effi.rpc.context.parameter.ParameterLinking;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.proxy.ProxyFactory;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeCapture;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static io.effi.rpc.boot.AnnotationSupport.annotationStyleParserForMethod;
import static io.effi.rpc.boot.AnnotationSupport.checkAnnotationStyle;

/**
 * Provide the annotation implementation of {@link CallerGroup}.
 */
public class AnnotationCallerGroup<T> extends AbstractPeerGroup<Caller<?>, T> implements CallerGroup<T>, InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationCallerGroup.class);

    private final CallGroup callGroup;

    private final AnnotationStyle annotationStyle;

    private Map<Method, MethodCaller> methodCallerMap;

    public AnnotationCallerGroup(Class<T> targetType, ScopedApplication application) {
        // todo 修改成module
        AssertUtil.notNull(application, "application");
        this.callGroup = ensureCallGroup(targetType);
        this.options = parseOption(callGroup, application);
        this.annotationStyle = checkAnnotationStyle(targetType, options);
        parseCaller(targetType, application);
        onInitialized(targetType, createProxy(application));
    }

    public CallGroup clientAnnotation() {
        return callGroup;
    }

    public AnnotationStyle annotationStyle() {
        return annotationStyle;
    }

    private CallGroup ensureCallGroup(Class<T> targetType) {
        AssertUtil.notNull(targetType, "targetType");
        AssertUtil.valid(targetType.isInterface(), "the target type must be an interface");
        return AssertUtil.requireAnnotation(targetType, CallGroup.class);
    }

    private HierarchicalOptions parseOption(CallGroup callGroup, ScopedApplication application) {
        HierarchicalOptions options = HierarchicalOptions.create()
                .withOwner(this)
                .withParent(application.callOptions());
        return AnnotationSupport.fillOption(callGroup, options);
    }

    private HierarchicalOptions parseCallOption(Method method) {
        HierarchicalOptions callOptions = HierarchicalOptions.create().withParent(options);
        Call call = method.getAnnotation(Call.class);
        return AnnotationSupport.fillOption(call, callOptions);
    }

    private void parseCaller(Class<T> targetType, ScopedApplication application) {
        List<Method> methods = AnnotationSupport.filterMethods(targetType.getMethods());
        methodCallerMap = new HashMap<>(methods.size());
        for (Method method : methods) {
            HierarchicalOptions callOption = parseCallOption(method);
            ScopedModule module = getModule(callOption, application);
            TransportProtocol protocol = getProtocol(callOption, application);
            if (protocol != null) {
                ReturnTypeWrapper returnTypeWrapper = getReturnType(method);
                var parameterMappers = getParameterMappers(callOption, method);
                Caller<?> caller = protocol.createCaller(returnTypeWrapper.typeCapture(), callOption, module);
                register(caller);
                methodCallerMap.put(method, new MethodCaller(caller, returnTypeWrapper.rpcType(), parameterMappers));
            }
        }
    }

    private T createProxy(ScopedApplication application) {
        String proxyName = proxy();
        ProxyFactory proxyFactory = application.platform().namedExtension(ProxyFactory.class, proxyName);
        return proxyFactory.createProxy(targetType, this);
    }

    private ScopedModule getModule(HierarchicalOptions options, ScopedApplication application) {
        String moduleName = options.option(Peer.ASSOCIATED_MODULE);
        ScopedModule module = application.lookupModule(moduleName);
        return module == null ? application.defaultModule() : module;
    }

    private ReturnTypeWrapper getReturnType(Method method) {
        Type returnType = method.getGenericReturnType();
        RpcType rpcType = RpcType.SYNC;
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType == CompletableFuture.class) {
                Type actualType = parameterizedType.getActualTypeArguments()[0];
                TypeCapture<?> typeCapture = TypeCapture.of(actualType);
                rpcType = RpcType.ASYNC;
                return new ReturnTypeWrapper(typeCapture, rpcType);
            }
        }
        return new ReturnTypeWrapper(TypeCapture.of(returnType), rpcType);
    }

    private ParameterLinking[] getParameterMappers(HierarchicalOptions options, Method method) {
        AnnotationStyleResolver methodAnnotationStyleResolver = annotationStyleParserForMethod(options, annotationStyle);
        ParameterLinking[] linkings;
        if (methodAnnotationStyleResolver != null && methodAnnotationStyleResolver.supports(method)) {
            linkings = methodAnnotationStyleResolver.resolveParameterLinking(method);
            methodAnnotationStyleResolver.resolveMethod(method, options);
        } else {
            linkings = ParameterLinking.emptyWrappers(method);
        }
        return linkings;
    }

    private TransportProtocol getProtocol(HierarchicalOptions options, ScopedApplication application) {
        String protocolName = options.option(Caller.PROTOCOL);
        if (StringUtil.isBlank(protocolName)) {
            return null;
        }
        return application.platform().namedExtension(TransportProtocol.class, protocolName);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, Callable<?> superInvoker) throws Throwable {
        MethodCaller methodCaller = methodCallerMap.get(method);
            if (methodCaller != null) {
                Caller<?> caller = methodCaller.caller();
                args = wrapArgs(methodCaller.linkings(), args, caller);
                RpcType rpcType = methodCaller.rpcType;
                if (rpcType == RpcType.SYNC) {
                    return caller.blockingCall(args);
                } else if (rpcType == RpcType.ASYNC) {
                    return caller.call(args);
                }
            }
        return null;
    }

    private Object[] wrapArgs(ParameterLinking[] linkings, Object[] args, Caller<?> caller) {
        if (CollectionUtil.isNotEmpty(args)) {
            Object[] result = new Object[linkings.length];
            for (int i = 0; i < linkings.length; i++) {
                ParameterLinking linking = linkings[i];
                AnnotationParameterWrapper<?> wrapper = linking.wrapper();
                if (wrapper != null) {
                    Parameter parameter = linking.parameter();
                    result[i] = wrapper.wrap(args[i], parameter, caller);
                }
            }
            return result;
        }
        return null;
    }

    @Override
    public String proxy() {
        return options.option(PROXY);
    }

    @Override
    public <R> R invoke(Caller<?> caller, Object... args) {
        return null;
    }

    private record MethodCaller(Caller<?> caller, RpcType rpcType,
                                ParameterLinking[] linkings) {}

    private record ReturnTypeWrapper(TypeCapture<?> typeCapture, RpcType rpcType) {}
}
