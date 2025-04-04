package io.effi.rpc.engine.builder;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.url.Config;
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

    protected CalleeBuilder(MethodMapper<?> methodMapper, Config config) {
        super(config);
        this.methodMapper = methodMapper;
    }

    /**
     * Sets callee description.
     *
     * @param desc
     * @return
     */
    public C desc(String desc) {
        config.set(DefaultConfigKeys.DESC.key(), desc);
        return returnThis();
    }

    /**
     * Exposes the current callee to the specified module(s).
     *
     * @param modules the specified module(s)
     */
    public C export(EffiRpcModule... modules) {
        CollectionUtil.addUnique(this.modules, modules);
        return returnThis();
    }

    /**
     * Returns the modules.
     */
    public List<EffiRpcModule> modules() {
        return modules;
    }

    /**
     * Returns the methodMapper.
     */
    public MethodMapper<?> methodMapper() {
        return methodMapper;
    }

}
