package io.effi.rpc.engine;

import io.effi.rpc.common.config.LinkedConfig;
import io.effi.rpc.common.config.NodeConfig;
import io.effi.rpc.common.reflect.MethodAccess;
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

    protected Class<T> targetType;

    protected T target;

    protected MethodAccess methodAccess;

    protected String name;

    public ComplexRemoteService(T target) {
        this(null, target);
    }

    public ComplexRemoteService(String name, T target) {
        this(name, target, null);
    }

    public ComplexRemoteService(String name, T target, LinkedConfig config) {
        initialize(name, target, config);
    }

    protected ComplexRemoteService() {

    }

    @SuppressWarnings("unchecked")
    protected void initialize(String name, T target, LinkedConfig config) {
        this.target = AssertUtil.notNull(target, "target");
        this.targetType = (Class<T>) ReflectionUtil.getTargetClass(target.getClass());
        if (StringUtil.isBlank(name)) name = ObjectUtil.lowercaseName(this.targetType);
        this.name = name;
        this.methodAccess = MethodAccess.get(this.targetType);
        this.config = config == null ? new NodeConfig(this) : config;
    }

    @Override
    public Callee<?> getCallee(String protocol, String path) {
        String key = GenerateUtil.generateInvokerKey(protocol, path);
        return getInvoker(key);
    }

    @Override
    public T target() {
        return target;
    }

    @Override
    public Class<T> targetType() {
        return targetType;
    }

    @Override
    public String name() {
        return name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R invokeCallee(Callee<T> callee, Object... args) {
        return (R) methodAccess.invoke(target, callee.methodIndex(), args);
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
        return methodAccess.getIndex(method.getName(), method.getParameterTypes());
    }

    @Override
    public String toString() {
        return "CombineRemoteService{target=" + target + ", name=" + name + "}";
    }
}
