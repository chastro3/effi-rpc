package io.effi.rpc.engine;

import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.common.config.HierarchicalNodeConfig;
import io.effi.rpc.common.compile.DynamicAccessor;
import io.effi.rpc.common.util.*;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.RemoteService;

import java.lang.reflect.Method;

/**
 * Default implementation of {@link RemoteService}.
 *
 * @param <T> the type of target
 */
public class ComplexRemoteService<T> extends AbstractInvokerContainer<Callee<?>> implements RemoteService<T> {

    protected Class<T> serviceType;

    protected T service;

    protected DynamicAccessor methodAccess;

    protected String name;

    public ComplexRemoteService(T service) {
        this(null, service);
    }

    public ComplexRemoteService(String name, T service) {
        this(name, service, null, null);
    }

    public ComplexRemoteService(String name, T service, Class<T> serviceType, NodeConfig config) {
        initialize(name, service, serviceType, config);
    }

    protected ComplexRemoteService() {

    }

    protected void initialize(String name, T service, Class<T> serviceType, NodeConfig config) {
        this.service = AssertUtil.notNull(service, "service");
        this.serviceType = checkServiceType(service, serviceType);
        this.name = checkName(name, this.serviceType);
        this.methodAccess = DynamicAccessor.get(this.serviceType);
        this.config = checkConfig(config);
    }

    @Override
    public Callee<?> getCallee(String protocol, String path) {
        String key = GenerateUtil.generateInvokerKey(protocol, path);
        return getInvoker(key);
    }

    @Override
    public T service() {
        return service;
    }

    @Override
    public Class<T> serviceType() {
        return serviceType;
    }

    @Override
    public String name() {
        return name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R invokeCallee(Callee<T> callee, Object... args) {
        return (R) methodAccess.invoke(service, callee.methodIndex(), args);
    }

    @Override
    public RemoteService<T> addCallee(Callee<?> callee) {
        String path = callee.queryPath() == null ? "" : callee.queryPath().path();
        String key = GenerateUtil.generateInvokerKey(callee.protocol(), path);
        addInvoker(key, callee);
        return this;
    }

    @Override
    public int getCalleeIndex(Callee<?> callee) {
        Method method = callee.method();
        return methodAccess.getMethodIndex(method.getName(), method.getParameterTypes());
    }

    @Override
    public String toString() {
        return "CombineRemoteService{target=" + service + ", name=" + name + "}";
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

    protected NodeConfig checkConfig(NodeConfig config) {
        if (config == null)
            config = new HierarchicalNodeConfig(this);
        return config;
    }
}
