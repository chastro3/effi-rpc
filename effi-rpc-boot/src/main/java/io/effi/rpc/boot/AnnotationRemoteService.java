package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.EffiRpcCallee;
import io.effi.rpc.annotation.rpc.EffiRpcService;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.config.DefaultHierarchicalConfig;
import io.effi.rpc.config.HierarchicalConfig;
import io.effi.rpc.context.RemoteService;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.annotation.AnnotationStyleParser;
import io.effi.rpc.context.parameter.MethodMapper;
import io.effi.rpc.context.parameter.ParameterMapper;
import io.effi.rpc.context.parameter.ParameterParser;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;

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

    public AnnotationRemoteService(T service, ScopedApplication application) {
        this(service, null, application);
    }

    public AnnotationRemoteService(T service, Class<T> serviceType, ScopedApplication application) {
        AssertUtil.notNull(application, "application");
        AssertUtil.notNull(service, "service");
        serviceType = checkServiceType(service, serviceType);
        EffiRpcService rpcService = ensureServiceAnnotation(serviceType);
        HierarchicalConfig config = parseConfig(rpcService, application);
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

    private HierarchicalConfig parseConfig(EffiRpcService effiRpcService, ScopedApplication application) {
        DefaultHierarchicalConfig config = new DefaultHierarchicalConfig(this, application.calleeConfig());
        return AnnotationSupport.fillConfig(effiRpcService, config);
    }

    private void parseCallee(ScopedApplication application) {
        List<Method> methods = AnnotationSupport.filterMethods(serviceType.getDeclaredMethods());
        for (Method method : methods) {
            DefaultHierarchicalConfig calleeConfig = parseCalleeConfig(method);
            MethodMapper<T> methodMapper = getMethodMapper(calleeConfig, method);
            ScopedModule module = getModule(calleeConfig, application);
            List<TransportProtocol> supportedProtocols = getSupportedProtocols(calleeConfig, application);
            if (CollectionUtil.isNotEmpty(supportedProtocols)) {
                supportedProtocols.forEach(protocol -> protocol.createCallee(methodMapper, calleeConfig, module));
            }
        }
    }

    private DefaultHierarchicalConfig parseCalleeConfig(Method method) {
        DefaultHierarchicalConfig config = new DefaultHierarchicalConfig(null, config());
        EffiRpcCallee effiRpcCallee = method.getAnnotation(EffiRpcCallee.class);
        return AnnotationSupport.fillConfig(effiRpcCallee, config);
    }

    private MethodMapper<T> getMethodMapper(DefaultHierarchicalConfig config, Method method) {
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

    private ScopedModule getModule(DefaultHierarchicalConfig config, ScopedApplication application) {
        String moduleName = config.get(ConfigNames.MODULE);
        ScopedModule module = application.lookupModule(moduleName);
        return module == null ? application.defaultModule() : module;
    }

    private List<TransportProtocol> getSupportedProtocols(DefaultHierarchicalConfig config, ScopedApplication application) {
        String[] protocolNames = config.get(ConfigNames.SUPPORTED_PROTOCOL);
        if (CollectionUtil.isEmpty(protocolNames)) {
            return Collections.emptyList();
        }
        List<TransportProtocol> result = new ArrayList<>();
        ScopedPlatform platform = application.platform();
        for (String protocolName : protocolNames) {
            TransportProtocol protocol = platform.namedExtension(TransportProtocol.class, protocolName);
            result.add(protocol);
        }
        return result;
    }

    private EffiRpcService ensureServiceAnnotation(Class<T> targetType) {
        return AssertUtil.requireAnnotation(targetType, EffiRpcService.class);
    }

}
