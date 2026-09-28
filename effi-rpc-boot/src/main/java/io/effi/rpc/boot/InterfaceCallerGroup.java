package io.effi.rpc.boot;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.parameter.ParameterLinking;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Method;

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

        private String protocolName;

        private Builder() {
        }

        public InterfaceCallerGroup.Builder<T> protocol(String protocolName) {
            this.protocolName = protocolName;
            return this;
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
            for (Method method : AnnotationSupport.filterMethods(targetType.getMethods())) {
                HierarchicalOptions methodOptions = HierarchicalOptions.create()
                        .withOwner(group)
                        .withParent(options);
                methodOptions.addOption(PATH, new String[]{methodPath(method)});
                ScopedModule methodModule = resolveModule(methodOptions);
                TransportProtocol protocol = protocol(methodModule);
                ReturnType returnType = returnType(method);
                ParameterLinking[] linkings = ParameterLinking.emptyWrappers(method);
                Caller<?> caller = protocol.createCaller(returnType.typeCapture(), methodOptions, methodModule);
                group.registerMethodCaller(method, caller, returnType.rpcType(), linkings);
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
