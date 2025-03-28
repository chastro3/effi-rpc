package io.effi.rpc.engine;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.extension.TypeToken;
import io.effi.rpc.common.extension.spi.ExtensionLoader;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.NetUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Locator;
import io.effi.rpc.contract.RemoteCaller;
import io.effi.rpc.contract.RpcType;
import io.effi.rpc.contract.annotation.AnnotationParameterWrapper;
import io.effi.rpc.contract.annotation.AnnotationStyleParser;
import io.effi.rpc.contract.annotation.EffiRpcCaller;
import io.effi.rpc.contract.annotation.EffiRpcClient;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.protocol.Protocol;
import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.proxy.ProxyFactory;
import io.effi.rpc.proxy.SuperInvoker;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.effi.rpc.engine.AnnotationSupport.annotationStyleParserForMethod;
import static io.effi.rpc.engine.AnnotationSupport.annotationStyleParserForType;

public class AnnotationRemoteCaller<T> extends AbstractInvokerContainer<Caller<?>> implements RemoteCaller<T>, InvocationHandler {

    private final EffiRpcClient clientAnnotation;

    private final Class<T> targetType;

    private final T proxy;

    private Map<Method, MethodCaller> methodCallerMap;

    public AnnotationRemoteCaller(Class<T> targetType, EffRpcApplication application) {
        this.clientAnnotation = parseClientAnnotation(targetType);
        AssertUtil.notNull(application, "application");
        this.targetType = targetType;
        this.config = parseConfig(clientAnnotation, application);
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

    /**
     * Returns the clientAnnotation.
     */
    public EffiRpcClient clientAnnotation() {
        return clientAnnotation;
    }

    private EffiRpcClient parseClientAnnotation(Class<T> targetType) {
        AssertUtil.notNull(targetType, "targetClass");
        if (!targetType.isInterface()) {
            throw new IllegalArgumentException("AnnotatedRemoteCaller can not be build because targetType is not interface");
        }
        if (!targetType.isAnnotationPresent(EffiRpcClient.class)) {
            throw new IllegalArgumentException("AnnotatedRemoteCaller can not be build because without @EffiRpcClient");
        }
        return targetType.getAnnotation(EffiRpcClient.class);
    }

    private Config parseConfig(EffiRpcClient effiRpcClient, EffRpcApplication application) {
        Config config = AnnotationSupport.toConfig(effiRpcClient);
        config.parent(application.consumerConfig());
        return config;
    }

    private Config parseCallerConfig(Method method) {
        EffiRpcCaller effiRpcCaller = method.getAnnotation(EffiRpcCaller.class);
        Config calleeConfig = AnnotationSupport.toConfig(effiRpcCaller);
        return calleeConfig.parent(config);
    }

    private void parseCaller(EffRpcApplication application) {
        String typeStyle = config.get(DefaultConfigKeys.STYLE);
        AnnotationStyleParser typeAnnotationStyleParser = annotationStyleParserForType(config, typeStyle, targetType);
        List<Method> methods = AnnotationSupport.filterMethods(targetType.getMethods());
        methodCallerMap = new HashMap<>(methods.size());
        for (Method method : methods) {
            Config callerConfig = parseCallerConfig(method);
            String address = callerConfig.get(DefaultConfigKeys.ADDRESS);
            Locator locator = null;
            if (StringUtil.isNotBlank(address)) {
                InetSocketAddress socketAddress = NetUtil.toInetSocketAddress(address);
                locator = DirectLocator.getInstance(socketAddress);
            } else {
                String remoteApplication = callerConfig.get(DefaultConfigKeys.APPLICATION);
                if (StringUtil.isNotBlank(remoteApplication)) {
                    locator = RegistryLocator.getInstance(remoteApplication);
                }
            }
            if (locator != null) {
                AnnotationStyleParser methodAnnotationStyleParser = annotationStyleParserForMethod(callerConfig, typeStyle, typeAnnotationStyleParser);
                ParameterMapper<AnnotationParameterWrapper<?>>[] parameterMappers;
                if (methodAnnotationStyleParser != null && methodAnnotationStyleParser.supported(method)) {
                    parameterMappers = methodAnnotationStyleParser.parseCallerParameterMapper(method);
                    methodAnnotationStyleParser.parseMethod(method, callerConfig);
                } else {
                    parameterMappers = ParameterMapper.emptyParsers(method);
                }
                String moduleName = callerConfig.get(DefaultConfigKeys.MODULE);
                EffiRpcModule module = application.getModule(moduleName);
                module = module == null ? application.defaultModule() : module;
                String protocolName = callerConfig.get(DefaultConfigKeys.PROTOCOL);
                if (StringUtil.isBlank(protocolName)) {
                    continue;
                }
                Protocol protocol = ExtensionLoader.loadExtension(Protocol.class, protocolName);
                ReturnTypeWrapper returnTypeWrapper = getReturnType(method);
                Caller<?> caller = protocol.createCaller(returnTypeWrapper.typeToken(), module, locator, callerConfig);
                addInvoker(caller.managerKey(), caller);
                methodCallerMap.put(method, new MethodCaller(caller, returnTypeWrapper.rpcType(), parameterMappers));
            }
        }
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

    private T createProxy() {
        String proxyName = config.get(DefaultConfigKeys.PROXY);
        ProxyFactory proxyFactory = ExtensionLoader.loadExtension(ProxyFactory.class, proxyName);
        return proxyFactory.createProxy(targetType, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args, SuperInvoker<?> superInvoker) throws Throwable {
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
