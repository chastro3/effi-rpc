package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.EffiRpcCaller;
import io.effi.rpc.annotation.rpc.EffiRpcClient;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.RemoteClient;
import io.effi.rpc.context.RpcType;
import io.effi.rpc.context.annotation.AnnotationParameterWrapper;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.annotation.AnnotationStyleParser;
import io.effi.rpc.context.parameter.ParameterMapper;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.DefaultHierarchicalConfig;
import io.effi.rpc.config.HierarchicalConfig;
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
 * Provide the annotation implementation of {@link RemoteClient}.
 */
public class AnnotationRemoteClient<T> extends AbstractPeerContainer<Caller<?>> implements RemoteClient<T>, InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationRemoteClient.class);

    private final EffiRpcClient clientAnnotation;

    private final AnnotationStyle annotationStyle;

    private final Class<T> targetType;

    private final T proxy;

    private Map<Method, MethodCaller> methodCallerMap;

    public AnnotationRemoteClient(Class<T> targetType, ScopedApplication application) {
        // todo 修改成module
        AssertUtil.notNull(application, "application");
        this.clientAnnotation = ensureClientAnnotation(targetType);
        this.targetType = targetType;
        this.config = parseConfig(clientAnnotation, application);
        this.annotationStyle = checkAnnotationStyle(targetType, config);
        parseCaller(application);
        this.proxy = createProxy(application);
    }

    @Override
    public Class<T> targetType() {
        return targetType;
    }

    @Override
    public T get() {
        return proxy;
    }

    public EffiRpcClient clientAnnotation() {
        return clientAnnotation;
    }

    public AnnotationStyle annotationStyle() {
        return annotationStyle;
    }

    private EffiRpcClient ensureClientAnnotation(Class<T> targetType) {
        AssertUtil.notNull(targetType, "targetType");
        AssertUtil.valid(targetType.isInterface(), "the target type must be an interface");
        return AssertUtil.requireAnnotation(targetType, EffiRpcClient.class);
    }

    private HierarchicalConfig parseConfig(EffiRpcClient effiRpcClient, ScopedApplication application) {
        DefaultHierarchicalConfig config = new DefaultHierarchicalConfig(this, application.callerConfig());
        return AnnotationSupport.fillConfig(effiRpcClient, config);
    }

    private DefaultHierarchicalConfig parseCallerConfig(Method method) {
        DefaultHierarchicalConfig config = new DefaultHierarchicalConfig(null, config());
        EffiRpcCaller rpcCaller = method.getAnnotation(EffiRpcCaller.class);
        return AnnotationSupport.fillConfig(rpcCaller, config);
    }

    private void parseCaller(ScopedApplication application) {
        List<Method> methods = AnnotationSupport.filterMethods(targetType.getMethods());
        methodCallerMap = new HashMap<>(methods.size());
        for (Method method : methods) {
            DefaultHierarchicalConfig callerConfig = parseCallerConfig(method);
            ScopedModule module = getModule(callerConfig, application);
            TransportProtocol protocol = getProtocol(callerConfig, application);
            if (protocol != null) {
                ReturnTypeWrapper returnTypeWrapper = getReturnType(method);
                var parameterMappers = getParameterMappers(callerConfig, method);
                Caller<?> caller = protocol.createCaller(returnTypeWrapper.typeCapture(), callerConfig, module);
                addPeer(caller.id(), caller);
                methodCallerMap.put(method, new MethodCaller(caller, returnTypeWrapper.rpcType(), parameterMappers));
            }
        }
    }

    private T createProxy(ScopedApplication application) {
        String proxyName = config.get(ConfigNames.PROXY);
        ProxyFactory proxyFactory = application.platform().namedExtension(ProxyFactory.class, proxyName);
        return proxyFactory.createProxy(targetType, this);
    }

    private ScopedModule getModule(DefaultHierarchicalConfig config, ScopedApplication application) {
        String moduleName = config.get(ConfigNames.MODULE);
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

    private ParameterMapper<AnnotationParameterWrapper<?>>[] getParameterMappers(DefaultHierarchicalConfig config, Method method) {
        AnnotationStyleParser methodAnnotationStyleParser = annotationStyleParserForMethod(config, annotationStyle);
        ParameterMapper<AnnotationParameterWrapper<?>>[] parameterMappers;
        if (methodAnnotationStyleParser != null && methodAnnotationStyleParser.supported(method)) {
            parameterMappers = methodAnnotationStyleParser.parseCallerParameterMapper(method);
            methodAnnotationStyleParser.parseMethod(method, config);
        } else {
            parameterMappers = ParameterMapper.emptyParsers(method);
        }
        return parameterMappers;
    }

    private TransportProtocol getProtocol(DefaultHierarchicalConfig config, ScopedApplication application) {
        String protocolName = config.get(ConfigNames.PROTOCOL);
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
                args = wrapArgs(methodCaller.parameterMappers(), args, caller);
                RpcType rpcType = methodCaller.rpcType;
                if (rpcType == RpcType.SYNC) {
                    return caller.blockingCall(args);
                } else if (rpcType == RpcType.ASYNC) {
                    return caller.call(args);
                }
            }
        return null;
    }

    private Object[] wrapArgs(ParameterMapper<AnnotationParameterWrapper<?>>[] parameterMappers, Object[] args, Caller<?> caller) {
        if (CollectionUtil.isNotEmpty(args)) {
            Object[] result = new Object[parameterMappers.length];
            for (int i = 0; i < parameterMappers.length; i++) {
                ParameterMapper<AnnotationParameterWrapper<?>> parameterMapper = parameterMappers[i];
                AnnotationParameterWrapper<?> wrapper = parameterMapper.value();
                if (wrapper != null) {
                    Parameter parameter = parameterMapper.parameter();
                    result[i] = wrapper.wrap(args[i], parameter, caller);
                }
            }
            return result;
        }
        return null;
    }

    private record MethodCaller(Caller<?> caller, RpcType rpcType,
                                ParameterMapper<AnnotationParameterWrapper<?>>[] parameterMappers) {}

    private record ReturnTypeWrapper(TypeCapture<?> typeCapture, RpcType rpcType) {}
}
