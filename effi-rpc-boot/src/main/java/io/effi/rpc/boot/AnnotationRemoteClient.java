package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.EffiRpcCaller;
import io.effi.rpc.annotation.rpc.EffiRpcClient;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.RemoteClient;
import io.effi.rpc.base.RpcType;
import io.effi.rpc.base.annotation.AnnotationParameterWrapper;
import io.effi.rpc.base.annotation.AnnotationStyle;
import io.effi.rpc.base.annotation.AnnotationStyleParser;
import io.effi.rpc.base.parameter.ParameterMapper;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.proxy.ProxyFactory;
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

import static io.effi.rpc.boot.AnnotationSupport.annotationStyleParserForMethod;
import static io.effi.rpc.boot.AnnotationSupport.checkAnnotationStyle;

/**
 * Provide the annotation implementation of {@link RemoteClient}.
 */
public class AnnotationRemoteClient<T> extends AbstractCallSideContainer<Caller<?>> implements RemoteClient<T>, InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationRemoteClient.class);

    private final EffiRpcClient clientAnnotation;

    private final AnnotationStyle annotationStyle;

    private final Class<T> targetType;

    private final T proxy;

    private Map<Method, MethodCaller> methodCallerMap;

    public AnnotationRemoteClient(Class<T> targetType, EffiRpcApplication application) {
        // todo 修改成module
        AssertUtil.notNull(application, "application");
        this.clientAnnotation = checkClientAnnotation(targetType);
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

    private EffiRpcClient checkClientAnnotation(Class<T> targetType) {
        AssertUtil.notNull(targetType, "targetType");
        AssertUtil.condition(targetType.isInterface(), "the target type must be an interface");
        return AssertUtil.notAnnotation(targetType, EffiRpcClient.class);
    }

    private NodeConfig parseConfig(EffiRpcClient effiRpcClient, EffiRpcApplication application) {
        HierarchicalNodeConfig config = new HierarchicalNodeConfig(this, application.consumerConfig());
        return AnnotationSupport.fillConfig(effiRpcClient, config);
    }

    private HierarchicalNodeConfig parseCallerConfig(Method method) {
        HierarchicalNodeConfig config = new HierarchicalNodeConfig(null, config());
        EffiRpcCaller rpcCaller = method.getAnnotation(EffiRpcCaller.class);
        return AnnotationSupport.fillConfig(rpcCaller, config);
    }

    private void parseCaller(EffiRpcApplication application) {
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
                addInvoker(caller.id(), caller);
                methodCallerMap.put(method, new MethodCaller(caller, returnTypeWrapper.rpcType(), parameterMappers));
            }
        }
    }

    private T createProxy(EffiRpcApplication application) {
        String proxyName = config.get(DefaultConfigNames.PROXY);
        ProxyFactory proxyFactory = application.platform().getExtension(ProxyFactory.class, proxyName);
        return proxyFactory.createProxy(targetType, this);
    }

    private EffiRpcModule getModule(HierarchicalNodeConfig config, EffiRpcApplication application) {
        String moduleName = config.get(DefaultConfigNames.MODULE);
        EffiRpcModule module = application.getModule(moduleName);
        return module == null ? application.defaultModule() : module;
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

    private Protocol getProtocol(HierarchicalNodeConfig config) {
        String protocolName = config.get(DefaultConfigNames.PROTOCOL);
        if (StringUtil.isBlank(protocolName)) {
            return null;
        }
        return TransportSupport.getProtocol(protocolName);
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
