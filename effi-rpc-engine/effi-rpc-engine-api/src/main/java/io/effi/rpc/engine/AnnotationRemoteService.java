package io.effi.rpc.engine;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.contract.RemoteService;
import io.effi.rpc.contract.annotation.AnnotationStyle;
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
import static io.effi.rpc.engine.AnnotationSupport.checkAnnotationStyle;

/**
 * Annotation implementation of {@link RemoteService}.
 */
public class AnnotationRemoteService<T> extends ComplexRemoteService<T> {

    private final EffiRpcService serviceAnnotation;

    private final AnnotationStyle annotationStyle;

    public AnnotationRemoteService(T service, EffRpcApplication application) {
        this(service, null, application);
    }

    public AnnotationRemoteService(T service, Class<T> serviceType, EffRpcApplication application) {
        AssertUtil.notNull(application, "application");
        AssertUtil.notNull(service, "service");
        serviceType = checkServiceType(service, serviceType);
        EffiRpcService rpcService = checkServiceAnnotation(serviceType);
        NodeConfig config = parseConfig(rpcService, application);
        initialize(rpcService.value(), service, serviceType, config);
        this.annotationStyle = checkAnnotationStyle(serviceType, config);
        this.serviceAnnotation = rpcService;
        parseCallee(application);
    }

    public EffiRpcService serviceAnnotation() {
        return serviceAnnotation;
    }

    public AnnotationStyle annotationStyle() {
        return annotationStyle;
    }

    private NodeConfig parseConfig(EffiRpcService effiRpcService, EffRpcApplication application) {
        HierarchicalNodeConfig config = new HierarchicalNodeConfig(this, application.providerConfig());
        return AnnotationSupport.fillConfig(effiRpcService, config);
    }

    private void parseCallee(EffRpcApplication application) {
        List<Method> methods = AnnotationSupport.filterMethods(serviceType.getDeclaredMethods());
        for (Method method : methods) {
            HierarchicalNodeConfig calleeConfig = parseCalleeConfig(method);
            MethodMapper<T> methodMapper = getMethodMapper(calleeConfig, method);
            EffiRpcModule[] modules = getModules(calleeConfig, application);
            List<Protocol> supportedProtocols = getSupportedProtocols(calleeConfig);
            if (CollectionUtil.isNotEmpty(supportedProtocols)) {
                supportedProtocols.forEach(protocol -> protocol.createCallee(methodMapper, calleeConfig, modules));
            }
        }
    }

    private HierarchicalNodeConfig parseCalleeConfig(Method method) {
        HierarchicalNodeConfig config = new HierarchicalNodeConfig(null, config());
        EffiRpcCallee effiRpcCallee = method.getAnnotation(EffiRpcCallee.class);
        return AnnotationSupport.fillConfig(effiRpcCallee, config);
    }

    private MethodMapper<T> getMethodMapper(HierarchicalNodeConfig config, Method method) {
        AnnotationStyleParser methodAnnotationStyleParser = annotationStyleParserForMethod(config, annotationStyle);
        ParameterMapper<ParameterParser<?>>[] parameterMappers;
        if (methodAnnotationStyleParser != null && methodAnnotationStyleParser.supported(method)) {
            parameterMappers = methodAnnotationStyleParser.parseCalleeParameterMapper(method);
            methodAnnotationStyleParser.parseMethod(method, config);
        } else {
            parameterMappers = ParameterMapper.emptyParsers(method);
        }
        return new MethodMapper<>(this, method, parameterMappers);
    }

    private EffiRpcModule[] getModules(HierarchicalNodeConfig config, EffRpcApplication application) {
        ArrayList<EffiRpcModule> result = new ArrayList<>();
        List<String> modules = config.getCascaded(DefaultConfigKeys.MODULES.key());
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

    private List<Protocol> getSupportedProtocols(HierarchicalNodeConfig config) {
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

    private EffiRpcService checkServiceAnnotation(Class<T> targetType) {
        EffiRpcService serviceAnnotation = targetType.getAnnotation(EffiRpcService.class);
        return AssertUtil.notNull(serviceAnnotation, "the target type is missing @EffiRpcService");
    }

}
