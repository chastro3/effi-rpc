package io.effi.rpc.boot;

import io.effi.rpc.annotation.rpc.Serve;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.annotation.AnnotationStyleResolver;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.context.parameter.PositionParameterBinder;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static io.effi.rpc.boot.AnnotationSupport.annotationStyleParserForMethod;
import static io.effi.rpc.boot.AnnotationSupport.checkAnnotationStyle;
import static io.effi.rpc.context.options.ServantOptions.DECLARED_PROTOCOL;

/**
 * Annotation-based implementation of {@link io.effi.rpc.context.ServantGroup}.
 */
public final class AnnotationServantGroup<T> extends DefaultServantGroup<T> {

    private final ServeGroup serviceAnnotation;

    private final AnnotationStyle annotationStyle;

    private AnnotationServantGroup(Builder<T> builder) {
        super(builder);
        this.serviceAnnotation = builder.serviceAnnotation;
        this.annotationStyle = builder.annotationStyle;
    }

    public static <T> AnnotationServantGroup.Builder<T> builder() {
        return new Builder<T>();
    }

    public ServeGroup serviceAnnotation() {
        return serviceAnnotation;
    }

    public AnnotationStyle annotationStyle() {
        return annotationStyle;
    }

    public static final class Builder<T> extends DefaultServantGroup.Builder<AnnotationServantGroup<T>, T, AnnotationServantGroup.Builder<T>> {

        private ServeGroup serviceAnnotation;

        private AnnotationStyle annotationStyle;

        private Builder() {
        }

        @Override
        protected void resolve() {
            super.resolve();
            if (options.parent() == null) {
                options.withParent(module.serveOptions());
            }
            serviceAnnotation = AssertUtil.requireAnnotation(targetType, ServeGroup.class);
            name = serviceAnnotation.value();
            AnnotationSupport.fillOption(serviceAnnotation, options);
            annotationStyle = checkAnnotationStyle(targetType, options);
        }

        @Override
        protected AnnotationServantGroup<T> newInstance() {
            return new AnnotationServantGroup<>(this);
        }

        @Override
        protected void resolveComponents(AnnotationServantGroup<T> group) {
            for (Method method : AnnotationSupport.filterMethods(targetType.getMethods())) {
                HierarchicalOptions methodOptions = HierarchicalOptions.create()
                        .withOwner(group)
                        .withParent(options);
                AnnotationSupport.fillOption(method.getAnnotation(Serve.class), methodOptions);
                ScopedModule methodModule = resolveModule(methodOptions);
                ServantMethod<T> servantMethod = new ServantMethod<>(group, method, methodBinding(methodOptions, method));
                for (TransportProtocol protocol : resolveProtocols(methodModule, methodOptions)) {
                    protocol.createServant(servantMethod, methodOptions, methodModule);
                }
            }
        }

        @Override
        protected void checkState(AnnotationServantGroup<T> group) {
            super.checkState(group);
            AssertUtil.notNull(group.target(), "target");
            AssertUtil.notNull(group.serviceAnnotation(), "serviceAnnotation");
            AssertUtil.notNull(group.annotationStyle(), "annotationStyle");
        }

        private List<TransportProtocol> resolveProtocols(ScopedModule module, HierarchicalOptions options) {
            String[] protocolNames = options.option(DECLARED_PROTOCOL);
            if (CollectionUtil.isEmpty(protocolNames)) {
                return List.of();
            }
            return Arrays.stream(protocolNames)
                    .map(name -> {
                        TransportProtocol protocol = module.platform()
                                .namedExtension(TransportProtocol.class, name);
                        return AssertUtil.notNull(protocol, "protocol");
                    })
                    .toList();
        }

        private MethodBinding methodBinding(HierarchicalOptions options, Method method) {
            AnnotationStyleResolver resolver = annotationStyleParserForMethod(options, annotationStyle);
            if (resolver != null && resolver.supports(method)) {
                MethodBinding binding = resolver.resolveMethodBinding(method);
                resolver.resolveMethod(method, options);
                return binding;
            }
            return MethodBinding.positional(method, PositionParameterBinder.INSTANCE);
        }
    }
}
