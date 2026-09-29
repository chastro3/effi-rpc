package io.effi.rpc.boot;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.parameter.MethodBinding;
import io.effi.rpc.context.parameter.PositionParameterBinder;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static io.effi.rpc.context.options.PeerOptions.PATH;

/**
 * Builds a caller group from a plain Java interface.
 */
public final class InterfaceCallerGroup<T> extends AbstractCallerGroup<T> {

    private InterfaceCallerGroup(Builder<T> builder) {
        super(builder);
    }

    public static <T> Builder<T> builder() {
        return new Builder<T>();
    }

    public static final class Builder<T> extends AbstractCallerGroup.Builder<InterfaceCallerGroup<T>, T, InterfaceCallerGroup.Builder<T>> {

        private Builder() {
        }

        @Override
        protected void validate() {
            super.validate();
            AssertUtil.notBlank(protocolName, "protocol");
        }

        @Override
        protected void resolve() {
            super.resolve();
            if (options.parent() == null) {
                options.withParent(module.callOptions());
            }
            resolveProxy();
        }

        @Override
        protected InterfaceCallerGroup<T> newInstance() {
            return new InterfaceCallerGroup<>(this);
        }

        @Override
        protected void resolveComponents(InterfaceCallerGroup<T> group) {
            Set<String> paths = new HashSet<>();
            for (Method method : AnnotationSupport.filterMethods(targetType.getMethods())) {
                String path = methodPath(method);
                AssertUtil.valid(paths.add(path), "Duplicate RPC method path: {}", path);
                HierarchicalOptions methodOptions = HierarchicalOptions.create()
                        .withOwner(group)
                        .withParent(options);
                methodOptions.addOption(PATH, new String[]{path});
                ScopedModule methodModule = resolveModule(methodOptions);
                TransportProtocol protocol = protocol(methodModule);
                ReturnType returnType = returnType(method);
                MethodBinding binding = MethodBinding.positional(method, PositionParameterBinder.INSTANCE);
                Caller<?> caller = protocol.createCaller(returnType.typeCapture(), methodOptions, methodModule);
                group.registerMethodCaller(method, caller, returnType.rpcType(), binding);
            }
        }

        private TransportProtocol protocol(ScopedModule module) {
            TransportProtocol protocol = module.platform().namedExtension(TransportProtocol.class, protocolName);
            if (protocol == null) {
                throw new IllegalArgumentException("Transport protocol not found: " + protocolName);
            }
            return protocol;
        }
    }
}
