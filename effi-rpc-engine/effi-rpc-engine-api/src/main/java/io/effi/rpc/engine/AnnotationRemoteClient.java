package io.effi.rpc.engine;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.RemoteClient;
import io.effi.rpc.contract.RpcType;
import io.effi.rpc.contract.annotation.*;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.proxy.ProxyFactory;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static io.effi.rpc.engine.AnnotationSupport.annotationStyleParserForMethod;
import static io.effi.rpc.engine.AnnotationSupport.checkAnnotationStyle;

/**
 * Annotation implementation of {@link RemoteClient}.
 */
public class AnnotationRemoteClient<T> extends AbstractInvokerContainer<Caller<?>> implements RemoteClient<T>, InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationRemoteClient.class);

    private final EffiRpcClient clientAnnotation;

    private final AnnotationStyle annotationStyle;

    private final Class<T> targetType;

    private final T proxy;

    private Map<Method, MethodCaller> methodCallerMap;

    public AnnotationRemoteClient(Class<T> targetType, EffRpcApplication application) {
        AssertUtil.notNull(application, "application");
        this.clientAnnotation = checkClientAnnotation(targetType);
        this.targetType = targetType;
        this.config = parseConfig(clientAnnotation, application);
        this.annotationStyle = checkAnnotationStyle(targetType, config);
        parseCaller(application);
        this.proxy = createProxy();
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

    private EffiRpcClient checkClientAnnotation(Class<T> targetType) {
        AssertUtil.notNull(targetType, "targetType");
        AssertUtil.condition(targetType.isInterface(), "the target type must be an interface");
        EffiRpcClient clientAnnotation = targetType.getAnnotation(EffiRpcClient.class);
        return AssertUtil.notNull(clientAnnotation, "the target is missing @EffiRpcClient");
    }

    private NodeConfig parseConfig(EffiRpcClient effiRpcClient, EffRpcApplication application) {
        HierarchicalNodeConfig config = new HierarchicalNodeConfig(this, application.consumerConfig());
        return AnnotationSupport.fillConfig(effiRpcClient, config);
    }

    private HierarchicalNodeConfig parseCallerConfig(Method method) {
        HierarchicalNodeConfig config = new HierarchicalNodeConfig(null, config());
        EffiRpcCaller rpcCaller = method.getAnnotation(EffiRpcCaller.class);
        return AnnotationSupport.fillConfig(rpcCaller, config);
    }

    private void parseCaller(EffRpcApplication application) {
        List<Method> methods = AnnotationSupport.filterMethods(targetType.getMethods());
        methodCallerMap = new HashMap<>(methods.size());
        for (Method method : methods) {
            HierarchicalNodeConfig callerConfig = parseCallerConfig(method);
            EffiRpcModule module = getModule(callerConfig, application);
            Protocol protocol = getProtocol(callerConfig);
            if (protocol != null) {
                ReturnTypeWrapper returnTypeWrapper = getReturnType(method);
                var parameterMappers = getParameterMappers(callerConfig, method);
                Caller<?> caller = protocol.createCaller(returnTypeWrapper.typeToken(), callerConfig, module);
                addInvoker(caller.repositoryKey(), caller);
                methodCallerMap.put(method, new MethodCaller(caller, returnTypeWrapper.rpcType(), parameterMappers));
            }
        }
    }

    private EffiRpcModule getModule(HierarchicalNodeConfig config, EffRpcApplication application) {
        String moduleName = config.get(DefaultConfigKeys.MODULE);
        EffiRpcModule module = application.getModule(moduleName);
        return module == null ? application.defaultModule() : module;
    }

    private Protocol getProtocol(HierarchicalNodeConfig config) {
        String protocolName = config.get(DefaultConfigKeys.PROTOCOL);
        if (StringUtil.isBlank(protocolName)) {
            return null;
        }
        return TransportSupport.getProtocol(protocolName);
    }

    private ReturnTypeWrapper getReturnType(Method method) {
        Type returnType = method.getGenericReturnType();
        RpcType rpcType = RpcType.SYNC;
        if (returnType instanceof ParameterizedType parameterizedType) {
            Type rawType = parameterizedType.getRawType();
            if (rawType == CompletableFuture.class) {
                Type actualType = parameterizedType.getActualTypeArguments()[0];
                TypeToken<?> typeToken = TypeToken.get(actualType);
                rpcType = RpcType.ASYNC;
                return new ReturnTypeWrapper(typeToken, rpcType);
            }
        }
        return new ReturnTypeWrapper(TypeToken.get(returnType), rpcType);
    }

    private ParameterMapper<AnnotationParameterWrapper<?>>[] getParameterMappers(HierarchicalNodeConfig config, Method method) {
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

    private T createProxy() {
        String proxyName = config.get(DefaultConfigKeys.PROXY);
        ProxyFactory proxyFactory = ExtensionLoader.loadExtension(ProxyFactory.class, proxyName);
        return proxyFactory.createProxy(targetType, this);
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

    private record ReturnTypeWrapper(TypeToken<?> typeToken, RpcType rpcType) {}
}
