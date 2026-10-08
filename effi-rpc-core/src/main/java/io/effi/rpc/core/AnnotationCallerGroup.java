package io.effi.rpc.core;

import io.effi.rpc.annotation.rpc.Call;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.annotation.AnnotationStyle;
import io.effi.rpc.context.annotation.AnnotationStyleResolver;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;

import java.lang.reflect.Method;

import static io.effi.rpc.context.options.CallerOptions.PROTOCOL;
import static io.effi.rpc.core.AnnotationSupport.annotationStyleParserForMethod;
import static io.effi.rpc.core.AnnotationSupport.checkAnnotationStyle;

/**
 * Provides the annotation-based implementation of {@link io.effi.rpc.context.CallerGroup}.
 */
public final class AnnotationCallerGroup<T> extends AbstractCallerGroup<T> {

    private final CallGroup callGroup;

    private final AnnotationStyle annotationStyle;

    private AnnotationCallerGroup(Builder<T> builder) {
        super(builder);
        this.callGroup = builder.callGroup;
        this.annotationStyle = builder.annotationStyle;
    }

    public static <T> AnnotationCallerGroup.Builder<T> builder() {
        return new Builder<T>();
    }

    public CallGroup clientAnnotation() {
        return callGroup;
    }

    public AnnotationStyle annotationStyle() {
        return annotationStyle;
    }

    public static final class Builder<T> extends AbstractCallerGroup.Builder<AnnotationCallerGroup<T>, T, AnnotationCallerGroup.Builder<T>> {

        private CallGroup callGroup;

        private AnnotationStyle annotationStyle;

        private Builder() {
        }

        @Override
        protected void resolve() {
            super.resolve();
            if (options.parent() == null) {
                options.withParent(module.callOptions());
            }
            callGroup = AssertUtil.requireAnnotation(targetType, CallGroup.class);
            AnnotationSupport.apply(callGroup, options);
            annotationStyle = checkAnnotationStyle(targetType, options, module.platform());
            resolveProxy();
        }

        @Override
        protected AnnotationCallerGroup<T> newInstance() {
            return new AnnotationCallerGroup<>(this);
        }

        @Override
        protected void resolveComponents(AnnotationCallerGroup<T> group) {
            for (Method method : AnnotationSupport.filterMethods(targetType.getMethods())) {
                HierarchicalOptions methodOptions = HierarchicalOptions.create()
                        .withOwner(group)
                        .withParent(options);
                AnnotationSupport.apply(method.getAnnotation(Call.class), methodOptions);
                ScopedModule methodModule = resolveModule(methodOptions);
                TransportProtocol protocol = resolveProtocol(methodModule, methodOptions);
                ReturnType returnType = returnType(method);
                MethodBinding binding = methodBinding(methodOptions, method);
                Caller<?> caller = protocol.createCaller(returnType.typeCapture(), methodOptions, methodModule);
                group.registerMethodCaller(method, caller, returnType.rpcType(), binding);
            }
        }

        @Override
        protected void checkState(AnnotationCallerGroup<T> group) {
            super.checkState(group);
            AssertUtil.notNull(group.proxy(), "proxy");
            AssertUtil.notNull(group.clientAnnotation(), "callGroup");
            AssertUtil.notNull(group.annotationStyle(), "annotationStyle");
        }

        private TransportProtocol resolveProtocol(ScopedModule module, HierarchicalOptions options) {
            String protocolName = options.option(PROTOCOL);
            if (StringUtil.isBlank(protocolName)) {
                protocolName = this.protocolName;
            }
            if (StringUtil.isBlank(protocolName)) {
                throw new IllegalStateException("No transport protocol configured for RPC method");
            }
            TransportProtocol protocol = module.platform().namedExtension(TransportProtocol.class, protocolName);
            AssertUtil.notNull(protocol, "protocol");
            return protocol;
        }

        private MethodBinding methodBinding(HierarchicalOptions options, Method method) {
            AnnotationStyleResolver resolver = annotationStyleParserForMethod(
                    options,
                    annotationStyle,
                    module.platform()
            );
            if (resolver != null && resolver.supports(method)) {
                MethodBinding binding = resolver.resolveMethodBinding(method);
                resolver.resolveMethod(method, options);
                return binding;
            }
            if (method.getParameterCount() > 0) {
                throw new IllegalStateException("No annotation style configured for RPC method parameters: " + method.toGenericString());
            }
            return MethodBinding.positional(method);
        }
    }
}
