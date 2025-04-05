package io.effi.rpc.engine;

import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.config.LinkedConfig;
import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.common.spi.ExtensionLoader;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.RemoteClient;
import io.effi.rpc.contract.RpcType;
import io.effi.rpc.contract.annotation.AnnotationParameterWrapper;
import io.effi.rpc.contract.annotation.AnnotationStyleParser;
import io.effi.rpc.contract.annotation.EffiRpcCaller;
import io.effi.rpc.contract.annotation.EffiRpcClient;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.proxy.ProxyFactory;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;

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

public class AnnotationRemoteClient<T> extends AbstractInvokerContainer<Caller<?>> implements RemoteClient<T>, InvocationHandler {

    private final EffiRpcClient clientAnnotation;

    private final AnnotationStyleWrapper styleWrapper;

    private final Class<T> targetType;

    private final T proxy;

    private Map<Method, MethodCaller> methodCallerMap;

    public AnnotationRemoteClient(Class<T> targetType, EffRpcApplication application) {
        this.clientAnnotation = parseClientAnnotation(targetType);
        AssertUtil.notNull(application, "application");
        this.targetType = targetType;
        this.config = parseConfig(clientAnnotation, application);
        this.styleWrapper = new AnnotationStyleWrapper(config);
        if (styleWrapper.parser() != null) styleWrapper.parser().parseType(targetType, config);
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

    public AnnotationStyleWrapper styleWrapper() {
        return styleWrapper;
    }

    private EffiRpcClient parseClientAnnotation(Class<T> targetType) {
        AssertUtil.notNull(targetType, "targetType");
        if (!targetType.isInterface()) {
            throw new IllegalArgumentException("AnnotatedRemoteCaller can not be build because targetType is not interface");
        }
        if (!targetType.isAnnotationPresent(EffiRpcClient.class)) {
            throw new IllegalArgumentException("AnnotatedRemoteCaller can not be build because without @EffiRpcClient");
        }
        return targetType.getAnnotation(EffiRpcClient.class);
    }

    private LinkedConfig parseConfig(EffiRpcClient effiRpcClient, EffRpcApplication application) {
        NodeConfig config = new NodeConfig(this, application.consumerConfig());
        AnnotationSupport.fillConfig(effiRpcClient, config);
        return config;
    }

    private NodeConfig parseCallerConfig(Method method) {
        NodeConfig config = new NodeConfig(null, config());
        EffiRpcCaller effiRpcCaller = method.getAnnotation(EffiRpcCaller.class);
        AnnotationSupport.fillConfig(effiRpcCaller, config);
        return config;
    }

    private void parseCaller(EffRpcApplication application) {
        List<Method> methods = AnnotationSupport.filterMethods(targetType.getMethods());
        methodCallerMap = new HashMap<>(methods.size());
        for (Method method : methods) {
            NodeConfig callerConfig = parseCallerConfig(method);
            EffiRpcModule module = getModule(callerConfig, application);
            Protocol protocol = getProtocol(callerConfig);
            if (protocol != null) {
                ReturnTypeWrapper returnTypeWrapper = getReturnType(method);
                var parameterMappers = getParameterMappers(callerConfig, method);
                Caller<?> caller = protocol.createCaller(returnTypeWrapper.typeToken(), callerConfig, module);
                addInvoker(caller.managerKey(), caller);
                methodCallerMap.put(method, new MethodCaller(caller, returnTypeWrapper.rpcType(), parameterMappers));
            }
        }
    }

    private EffiRpcModule getModule(NodeConfig config, EffRpcApplication application) {
        String moduleName = config.get(DefaultConfigKeys.MODULE);
        EffiRpcModule module = application.getModule(moduleName);
        return module == null ? application.defaultModule() : module;
    }

    private Protocol getProtocol(NodeConfig config) {
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

    private ParameterMapper<AnnotationParameterWrapper<?>>[] getParameterMappers(NodeConfig config, Method method) {
        AnnotationStyleParser methodAnnotationStyleParser = annotationStyleParserForMethod(config, styleWrapper);
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

    private record MethodCaller(Caller<?> caller,
                                RpcType rpcType,
                                ParameterMapper<AnnotationParameterWrapper<?>>[] parameterMappers) {

    }

    private record ReturnTypeWrapper(TypeToken<?> typeToken, RpcType rpcType) {}
}
