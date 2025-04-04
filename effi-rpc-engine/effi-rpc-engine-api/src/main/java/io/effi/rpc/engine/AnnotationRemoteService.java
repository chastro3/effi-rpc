package io.effi.rpc.engine;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.ReflectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.RemoteService;
import io.effi.rpc.contract.annotation.AnnotationStyleParser;
import io.effi.rpc.contract.annotation.EffiRpcCallee;
import io.effi.rpc.contract.annotation.EffiRpcService;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.contract.parameter.ParameterParser;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.effi.rpc.engine.AnnotationSupport.annotationStyleParserForMethod;

/**
 * Annotation implementation of {@link RemoteService}.
 *
 * @param <T> the type of target
 */
public class AnnotationRemoteService<T> extends ComplexRemoteService<T> {

    private final EffiRpcService serviceAnnotation;

    private final AnnotationStyleWrapper styleWrapper;

    public AnnotationRemoteService(T service, EffRpcApplication application) {
        this.serviceAnnotation = parseServiceAnnotation(service);
        AssertUtil.notNull(application, "application");
        initialize(serviceAnnotation.value(), service, parseConfig(serviceAnnotation, application));
        this.styleWrapper = new AnnotationStyleWrapper(config);
        if (styleWrapper.parser() != null) styleWrapper.parser().parseType(targetType, config);
        parseCallee(application);
    }

    public EffiRpcService serviceAnnotation() {
        return serviceAnnotation;
    }

    public AnnotationStyleWrapper styleWrapper() {
        return styleWrapper;
    }

    private EffiRpcService parseServiceAnnotation(T service) {
        AssertUtil.notNull(service, "service");
        Class<?> targetClass = ReflectionUtil.getTargetClass(service.getClass());
        if (!targetClass.isAnnotationPresent(EffiRpcService.class)) {
            throw new IllegalArgumentException("AnnotatedRemoteService can not be build because without @EffiRpcService");
        }
        return targetClass.getAnnotation(EffiRpcService.class);
    }

    private Config parseConfig(EffiRpcService effiRpcService, EffRpcApplication application) {
        Config config = AnnotationSupport.toConfig(effiRpcService);
        config.parent(application.providerConfig());
        return config;
    }

    private void parseCallee(EffRpcApplication application) {
        List<Method> methods = AnnotationSupport.filterMethods(targetType.getMethods());
        for (Method method : methods) {
            Config calleeConfig = parseCalleeConfig(method);
            MethodMapper<T> methodMapper = getMethodMapper(calleeConfig, method);
            EffiRpcModule[] modules = getModules(calleeConfig, application);
            List<Protocol> supportedProtocols = getSupportedProtocols(calleeConfig);
            if(CollectionUtil.isNotEmpty(supportedProtocols)){
                supportedProtocols.forEach(protocol -> protocol.createCallee(methodMapper, calleeConfig, modules));
            }
        }
    }

    private Config parseCalleeConfig(Method method) {
        EffiRpcCallee effiRpcCallee = method.getAnnotation(EffiRpcCallee.class);
        Config calleeConfig = AnnotationSupport.toConfig(effiRpcCallee);
        return calleeConfig.parent(config);
    }

    private MethodMapper<T> getMethodMapper(Config config, Method method) {
        AnnotationStyleParser methodAnnotationStyleParser = annotationStyleParserForMethod(config, styleWrapper);
        ParameterMapper<ParameterParser<?>>[] parameterMappers;
        if (methodAnnotationStyleParser != null && methodAnnotationStyleParser.supported(method)) {
            parameterMappers = methodAnnotationStyleParser.parseCalleeParameterMapper(method);
            methodAnnotationStyleParser.parseMethod(method, config);
        } else {
            parameterMappers = ParameterMapper.emptyParsers(method);
        }
        return new MethodMapper<>(this, method, parameterMappers);
    }

    private EffiRpcModule[] getModules(Config config, EffRpcApplication application) {
        ArrayList<EffiRpcModule> result = new ArrayList<>();
        List<String> modules = config.getMerged(DefaultConfigKeys.MODULES.key());
        if (CollectionUtil.isNotEmpty(modules)) {
            for (EffiRpcModule rpcModule : application.modules()) {
                if (modules.contains(rpcModule.name())) {
                    result.add(rpcModule);
                }
            }
        }
        if (result.isEmpty()) result.add(application.defaultModule());
        return result.toArray(new EffiRpcModule[0]);
    }

    private List<Protocol> getSupportedProtocols(Config config) {
        String protocolNames = config.get(DefaultConfigKeys.PROTOCOL);
        if (StringUtil.isBlank(protocolNames)) {
            return Collections.emptyList();
        }
        String[] protocols = protocolNames.split(",");
        List<Protocol> result = new ArrayList<>();
        for (String protocolName : protocols) {
            Protocol protocol = TransportSupport.getProtocol(protocolName);
            result.add(protocol);
        }
        return result;
    }

}
