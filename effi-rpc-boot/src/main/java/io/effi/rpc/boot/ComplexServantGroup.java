package io.effi.rpc.boot;

import io.effi.rpc.compile.DynamicAccessor;
import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.ServantGroup;
import io.effi.rpc.util.ObjectUtil;
import io.effi.rpc.util.ReflectionUtil;
import io.effi.rpc.util.StringUtil;

import java.lang.reflect.Method;

/**
 * Provide the default implementation of {@link ServantGroup}.
 */
public class ComplexServantGroup<T> extends AbstractPeerGroup<Servant, T> implements ServantGroup<T> {

    protected DynamicAccessor methodAccess;

    protected String name;

    protected ComplexServantGroup() {
    }

    public ComplexServantGroup(T service) {
        this(null, service);
    }

    public ComplexServantGroup(String name, T service) {
        this(name, service, null, null);
    }

    public ComplexServantGroup(String name, T service, Class<T> serviceType, HierarchicalOptions options) {
        initialize(name, service, serviceType, options);
    }

    protected void initialize(String name, T service, Class<T> serviceType, HierarchicalOptions options) {
        serviceType = checkServiceType(service, serviceType);
        this.name = checkName(name, serviceType);
        this.methodAccess = DynamicAccessor.fetch(serviceType);
        this.options = checkOptions(options);
        onInitialized(serviceType, service);
    }


    @Override
    public String name() {
        return name;
    }


    @Override
    public int indexOf(Servant servant) {
        Method method = servant.method();
        return methodAccess.findMethodIndex(method.getName(), method.getParameterTypes());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R invoke(Servant servant, Object... args) {
        return (R) methodAccess.invoke(target, servant.methodIndex(), args);
    }

    @Override
    public String toString() {
        return "target=" + target + ", id=" + name;
    }

    @SuppressWarnings("unchecked")
    protected Class<T> checkServiceType(T service, Class<T> serviceType) {
        if (serviceType == null)
            serviceType = (Class<T>) ReflectionUtil.getTargetClass(service.getClass());
        return serviceType;
    }

    protected String checkName(String name, Class<T> serviceType) {
        if (StringUtil.isBlank(name))
            name = ObjectUtil.lowercaseName(serviceType);
        return name;
    }

    protected HierarchicalOptions checkOptions(HierarchicalOptions options) {
        if (options == null)
            options = HierarchicalOptions.create().withOwner(this);
        return options;
    }
}
