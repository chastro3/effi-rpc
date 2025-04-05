package io.effi.rpc.engine.builder;

import io.effi.rpc.common.config.DefaultConfigKeys;
import io.effi.rpc.common.config.LinkedConfig;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.MethodMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for creating {@link Callee} instances,defining settings for callee.
 *
 * @param <T> The type of {@link Callee}.
 * @param <C> The type of the builder.
 */
public abstract class CalleeBuilder<T extends Callee<?>, C extends CalleeBuilder<T, C>>
        extends InvokerBuilder<T, C> {

    protected MethodMapper<?> methodMapper;

    protected List<EffiRpcModule> modules = new ArrayList<>();

    protected CalleeBuilder(MethodMapper<?> methodMapper, LinkedConfig config) {
        super(config);
        this.methodMapper = AssertUtil.notNull(methodMapper, "methodMapper");
        this.container = methodMapper.remoteService();
    }

    /**
     * Sets callee description.
     */
    public C desc(String desc) {
        config.set(DefaultConfigKeys.CALLEE_DESC, desc);
        return returnThis();
    }

    /**
     * Exposes the current callee to the specified module(s).
     */
    public C export(EffiRpcModule... modules) {
        CollectionUtil.addUnique(this.modules, modules);
        return returnThis();
    }

    public List<EffiRpcModule> modules() {
        return modules;
    }

    public MethodMapper<?> methodMapper() {
        return methodMapper;
    }

}
