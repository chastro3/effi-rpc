package io.effi.rpc.boot;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.context.parameter.PositionParameterBinder;
import io.effi.rpc.context.parameter.ServantMethod;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static io.effi.rpc.context.options.PeerOptions.PATH;

/**
 * Builds a servant group from a plain Java interface and its implementation.
 */
public final class InterfaceServantGroup<T> extends DefaultServantGroup<T> {

    private InterfaceServantGroup(Builder<T> builder) {
        super(builder);
    }

    public static <T> InterfaceServantGroup.Builder<T> builder() {
        return new Builder<T>();
    }

    private static Method targetMethod(Object target, Method interfaceMethod) {
        try {
            return target.getClass().getMethod(
                    interfaceMethod.getName(),
                    interfaceMethod.getParameterTypes()
            );
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Implementation method not found: " + interfaceMethod.toGenericString(), e);
        }
    }

    public static final class Builder<T> extends DefaultServantGroup.Builder<InterfaceServantGroup<T>, T, InterfaceServantGroup.Builder<T>> {

        private Builder() {
        }

        @Override
        protected void validate() {
            super.validate();
            AssertUtil.valid(targetType.isInterface(), "targetType must be an interface");
            AssertUtil.notBlank(protocolName, "protocol");
        }

        @Override
        protected void resolve() {
            super.resolve();
            if (options.parent() == null) {
                options.withParent(module.serveOptions());
            }
            name = targetType.getName();
        }

        @Override
        protected InterfaceServantGroup<T> newInstance() {
            return new InterfaceServantGroup<>(this);
        }

        @Override
        protected void resolveComponents(InterfaceServantGroup<T> group) {
            TransportProtocol protocol = resolveProtocol();
            Set<String> paths = new HashSet<>();
            for (Method interfaceMethod : AnnotationSupport.filterMethods(targetType.getMethods())) {
                String path = methodPath(interfaceMethod);
                AssertUtil.valid(paths.add(path), "Duplicate RPC method path: {}", path);
                HierarchicalOptions methodOptions = HierarchicalOptions.create()
                        .withOwner(group)
                        .withParent(options);
                methodOptions.addOption(PATH, new String[]{path});
                ScopedModule methodModule = resolveModule(methodOptions);
                Method targetMethod = targetMethod(service, interfaceMethod);
                MethodBinding binding = MethodBinding.positional(interfaceMethod, PositionParameterBinder.INSTANCE);
                ServantMethod<T> servantMethod = new ServantMethod<>(group, targetMethod, binding);
                protocol.createServant(servantMethod, methodOptions, methodModule);
            }
        }

        private TransportProtocol resolveProtocol() {
            TransportProtocol protocol = module.platform().namedExtension(TransportProtocol.class, protocolName);
            if (protocol == null) {
                throw new IllegalArgumentException(
                        "Transport protocol not found: " + protocolName
                );
            }
            return protocol;
        }
    }
}
