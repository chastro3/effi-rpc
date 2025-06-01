package io.effi.rpc.boot;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.HierarchicalNodeConfig;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.base.RemoteService;
import io.effi.rpc.base.annotation.AnnotationStyle;
import io.effi.rpc.base.annotation.AnnotationStyleParser;
import io.effi.rpc.annotation.rpc.EffiRpcCallee;
import io.effi.rpc.annotation.rpc.EffiRpcService;
import io.effi.rpc.component.EffiRpcApplication;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.base.parameter.ParameterMapper;
import io.effi.rpc.base.parameter.ParameterParser;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.effi.rpc.boot.AnnotationSupport.annotationStyleParserForMethod;
import static io.effi.rpc.boot.AnnotationSupport.checkAnnotationStyle;

/**
 * Provide the annotation implementation of {@link RemoteService}.
 */
public class AnnotationRemoteService<T> extends ComplexRemoteService<T> {

    private final EffiRpcService serviceAnnotation;

    private final AnnotationStyle annotationStyle;

    public AnnotationRemoteService(T service, EffiRpcApplication application) {
        this(service, null, application);
    }

    public AnnotationRemoteService(T service, Class<T> serviceType, EffiRpcApplication application) {
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

    private NodeConfig parseConfig(EffiRpcService effiRpcService, EffiRpcApplication application) {
        HierarchicalNodeConfig config = new HierarchicalNodeConfig(this, application.providerConfig());
        return AnnotationSupport.fillConfig(effiRpcService, config);
    }

    private void parseCallee(EffiRpcApplication application) {
        List<Method> methods = AnnotationSupport.filterMethods(serviceType.getDeclaredMethods());
        for (Method method : methods) {
            HierarchicalNodeConfig calleeConfig = parseCalleeConfig(method);
            MethodMapper<T> methodMapper = getMethodMapper(calleeConfig, method);
            EffiRpcModule module = getModule(calleeConfig, application);
            List<Protocol> supportedProtocols = getSupportedProtocols(calleeConfig);
            if (CollectionUtil.isNotEmpty(supportedProtocols)) {
                supportedProtocols.forEach(protocol -> protocol.createCallee(methodMapper, calleeConfig, module));
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

    private EffiRpcModule getModule(HierarchicalNodeConfig config, EffiRpcApplication application) {
        String moduleName = config.get(DefaultConfigKeys.MODULE);
        EffiRpcModule module = application.getModule(moduleName);
        return module == null ? application.defaultModule() : module;
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
