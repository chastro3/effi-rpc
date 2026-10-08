package io.effi.rpc.core;

import io.effi.rpc.compile.DynamicAccessor;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.ServantGroup;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.ReflectionUtil;
import io.effi.rpc.util.StringUtil;

import java.lang.reflect.Method;

import static io.effi.rpc.component.serialization.options.SerializationOptions.SERIALIZER;

/**
 * Provides the default implementation of {@link ServantGroup}.
 */
public class DefaultServantGroup<T> extends AbstractPeerGroup<Servant, T> implements ServantGroup<T> {

    protected DynamicAccessor methodAccess;

    protected T service;

    protected String name;

    protected DefaultServantGroup() {
    }

    protected DefaultServantGroup(DefaultServantGroup.Builder<?, T, ?> builder) {
        super(builder);
        this.service = builder.service;
        this.name = checkName(builder.name, targetType);
        this.methodAccess = DynamicAccessor.fetch(targetType);
    }

    protected String checkName(String name, Class<T> targetType) {
        if (StringUtil.isBlank(name))
            name = ObjectUtil.lowercaseName(targetType);
        return name;
    }

    public DefaultServantGroup(T service) {
        this(null, service);
    }

    public DefaultServantGroup(String name, T service) {
        this(name, service, null, null);
    }

    public DefaultServantGroup(String name, T service, Class<T> targetType, HierarchicalOptions options) {
        initialize(name, service, targetType, options);
    }

    protected void initialize(String name, T service, Class<T> targetType, HierarchicalOptions options) {
        targetType = checkTargetType(service, targetType);
        this.service = service;
        this.name = checkName(name, targetType);
        this.methodAccess = DynamicAccessor.fetch(targetType);
        this.options = checkOptions(options);
        onInitialized(targetType);
    }

    @SuppressWarnings("unchecked")
    protected Class<T> checkTargetType(T service, Class<T> targetType) {
        if (targetType == null)
            targetType = (Class<T>) ReflectionUtil.getTargetClass(service.getClass());
        return targetType;
    }

    protected HierarchicalOptions checkOptions(HierarchicalOptions options) {
        if (options == null) {
            options = HierarchicalOptions.create().withOwner(this);
        } else {
            options.withOwner(this);
        }
        return options;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public T service() {
        return service;
    }

    @Override
    public int indexOf(Servant servant) {
        Method method = servant.method();
        return methodAccess.findMethodIndex(method.getName(), method.getParameterTypes());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R invoke(Servant servant, Object... args) {
        return (R) methodAccess.invoke(service, servant.methodIndex(), args);
    }

    @Override
    public String toString() {
        return "service=" + service + ", id=" + name;
    }

    /**
     * Assembles a servant group with an optional explicit service type.
     */
    public abstract static class Builder<G extends DefaultServantGroup<T>, T, SELF extends Builder<G, T, SELF>>
            extends AbstractPeerGroup.Builder<G, Servant, T, SELF> {

        protected T service;

        protected String protocolName;

        public SELF service(T service) {
            this.service = service;
            return self();
        }

        public SELF protocol(String protocolName) {
            this.protocolName = AssertUtil.notBlank(protocolName, "protocol");
            return self();
        }

        public SELF serializer(String serializer) {
            addOption(SERIALIZER, serializer);
            return self();
        }

        @Override
        protected void validate() {
            AssertUtil.notNull(module, "module");
            AssertUtil.notNull(service, "service");
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void resolve() {
            if (targetType == null) {
                targetType = (Class<T>) ReflectionUtil.getTargetClass(service.getClass());
            }
        }
    }
}
