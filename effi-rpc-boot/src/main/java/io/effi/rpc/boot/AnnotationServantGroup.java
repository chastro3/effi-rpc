package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.Serve;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.ServantGroup;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.annotation.AnnotationStyleResolver;
import io.effi.rpc.context.parameter.ParameterBinding;
import io.effi.rpc.context.parameter.ServantMethod;
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
 * Provide the annotation implementation of {@link ServantGroup}.
 */
public class AnnotationServantGroup<T> extends ComplexServantGroup<T> {

    private final ServeGroup serviceAnnotation;

    private final AnnotationStyle annotationStyle;

    public AnnotationServantGroup(T service, ScopedApplication application) {
        this(service, null, application);
    }

    public AnnotationServantGroup(T service, Class<T> serviceType, ScopedApplication application) {
        AssertUtil.notNull(application, "application");
        AssertUtil.notNull(service, "service");
        serviceType = checkServiceType(service, serviceType);
        ServeGroup rpcService = ensureServiceAnnotation(serviceType);
        HierarchicalOptions options = parseOptions(rpcService, application);
        initialize(rpcService.value(), service, serviceType, options);
        this.annotationStyle = checkAnnotationStyle(serviceType, options);
        this.serviceAnnotation = rpcService;
        parseServant(serviceType, application);
    }

    public ServeGroup serviceAnnotation() {
        return serviceAnnotation;
    }

    public AnnotationStyle annotationStyle() {
        return annotationStyle;
    }

    private HierarchicalOptions parseOptions(ServeGroup serveGroup, ScopedApplication application) {
        HierarchicalOptions options = HierarchicalOptions.create()
                .withOwner(this)
                .withParent(application.serveOptions());
        return AnnotationSupport.fillOption(serveGroup, options);
    }

    private void parseServant(Class<T> serviceType, ScopedApplication application) {
        List<Method> methods = AnnotationSupport.filterMethods(serviceType.getMethods());
        for (Method method : methods) {
            HierarchicalOptions serveOptions = parseServeOption(method);
            ServantMethod<T> servantMethod = getMethodMapper(serveOptions, method);
            ScopedModule module = getModule(serveOptions, application);
            List<TransportProtocol> supportedProtocols = getSupportedProtocols(serveOptions, application);
            if (CollectionUtil.isNotEmpty(supportedProtocols)) {
                supportedProtocols.forEach(protocol -> protocol.createServant(servantMethod, serveOptions, module));
            }
        }
    }

    private HierarchicalOptions parseServeOption(Method method) {
        HierarchicalOptions serveOptions = HierarchicalOptions.create().withParent(options);
        Serve serve = method.getAnnotation(Serve.class);
        return AnnotationSupport.fillOption(serve, serveOptions);
    }

    private ServantMethod<T> getMethodMapper(HierarchicalOptions options, Method method) {
        AnnotationStyleResolver methodAnnotationStyleResolver = annotationStyleParserForMethod(options, annotationStyle);
        ParameterBinding[] bindings;
        if (methodAnnotationStyleResolver != null && methodAnnotationStyleResolver.supports(method)) {
            bindings = methodAnnotationStyleResolver.resolveParameterBinding(method);
            methodAnnotationStyleResolver.resolveMethod(method, options);
        } else {
            bindings = ParameterBinding.emptyResolvers(method);
        }
        return new ServantMethod<>(this, method, bindings);
    }

    private ScopedModule getModule(HierarchicalOptions options, ScopedApplication application) {
        String moduleName = options.option(Peer.ASSOCIATED_MODULE);
        ScopedModule module = application.lookupModule(moduleName);
        return module == null ? application.defaultModule() : module;
    }

    private List<TransportProtocol> getSupportedProtocols(HierarchicalOptions options, ScopedApplication application) {
        String[] protocolNames = options.option(Servant.DECLARED_PROTOCOL);
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

    private ServeGroup ensureServiceAnnotation(Class<T> targetType) {
        return AssertUtil.requireAnnotation(targetType, ServeGroup.class);
    }

}
