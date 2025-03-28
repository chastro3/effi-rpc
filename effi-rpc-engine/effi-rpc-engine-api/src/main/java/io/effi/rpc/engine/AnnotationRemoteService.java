package io.effi.rpc.engine;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.extension.spi.ExtensionLoader;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.ReflectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.RemoteService;
import io.effi.rpc.contract.annotation.AnnotationStyleParser;
import io.effi.rpc.contract.annotation.EffiRpcCallee;
import io.effi.rpc.contract.annotation.EffiRpcService;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.module.ServerExporter;
import io.effi.rpc.contract.parameter.MethodMapper;
import io.effi.rpc.contract.parameter.ParameterMapper;
import io.effi.rpc.contract.parameter.ParameterParser;
import io.effi.rpc.protocol.Protocol;

import java.lang.reflect.Method;
import java.util.List;

import static io.effi.rpc.engine.AnnotationSupport.annotationStyleParserForMethod;
import static io.effi.rpc.engine.AnnotationSupport.annotationStyleParserForType;

/**
 * Annotation implementation of {@link RemoteService}.
 *
 * @param <T> the type of target
 */
public class AnnotationRemoteService<T> extends ComplexRemoteService<T> {

    private final EffiRpcService serviceAnnotation;

    public AnnotationRemoteService(T service, EffRpcApplication application) {
        this.serviceAnnotation = parseServiceAnnotation(service);
        AssertUtil.notNull(application, "application");
        initialize(serviceAnnotation.value(), service, parseConfig(serviceAnnotation, application));
        parseCallee(application);
    }

    /**
     * Returns the serviceAnnotation.
     *
     * @return the serviceAnnotation
     */
    public EffiRpcService serviceAnnotation() {
        return serviceAnnotation;
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
        String typeStyle = config.get(DefaultConfigKeys.STYLE);
        AnnotationStyleParser typeAnnotationStyleParser = annotationStyleParserForType(config, typeStyle, targetType);
        List<Method> methods = AnnotationSupport.filterMethods(targetType.getMethods());
        for (Method method : methods) {
            Config calleeConfig = parseCalleeConfig(method);
            AnnotationStyleParser methodAnnotationStyleParser = annotationStyleParserForMethod(calleeConfig, typeStyle, typeAnnotationStyleParser);
            ParameterMapper<ParameterParser<?>>[] parameterMappers;
            if (methodAnnotationStyleParser != null && methodAnnotationStyleParser.supported(method)) {
                parameterMappers = methodAnnotationStyleParser.parseCalleeParameterMapper(method);
                methodAnnotationStyleParser.parseMethod(method, calleeConfig);
            } else {
                parameterMappers = ParameterMapper.emptyParsers(method);
            }
            String protocolNames = calleeConfig.get(DefaultConfigKeys.PROTOCOL);
            if (StringUtil.isBlank(protocolNames)) {
                continue;
            }
            String[] protocols = protocolNames.split(",");
            for (String protocolName : protocols) {
                Protocol protocol = ExtensionLoader.loadExtension(Protocol.class, protocolName);
                MethodMapper<T> methodMapper = new MethodMapper<>(this, method, parameterMappers);
                Callee<T> callee = protocol.createCallee(methodMapper, calleeConfig);
                List<String> modules = callee.getMerged(DefaultConfigKeys.MODULES);
                List<String> excludePorts = callee.config().getMerged(DefaultConfigKeys.EXCLUDED_PORT.key());
                boolean hasMatchedModule = false;
                if (CollectionUtil.isNotEmpty(modules)) {
                    for (EffiRpcModule rpcModule : application.modules()) {
                        if (modules.contains(rpcModule.name())) {
                            hasMatchedModule = true;
                            registerCallee(rpcModule, callee, excludePorts);
                        }
                    }
                    if (!hasMatchedModule) {
                        registerCallee(application.defaultModule(), callee, excludePorts);
                    }
                }
            }
        }
    }

    private Config parseCalleeConfig(Method method) {
        EffiRpcCallee effiRpcCallee = method.getAnnotation(EffiRpcCallee.class);
        Config calleeConfig = AnnotationSupport.toConfig(effiRpcCallee);
        return calleeConfig.parent(config);
    }

    private void registerCallee(EffiRpcModule module, Callee<?> callee, List<String> excludedPorts) {
        callee.export(module);
        for (ServerExporter serverExporter : module.serverExporterManager().values()) {
            URL url = serverExporter.url();
            if (callee.protocol().equals(url.protocol()) && !excludedPorts.contains(String.valueOf(url.port()))) {
                serverExporter.callee(callee);
            }
        }
    }

}
