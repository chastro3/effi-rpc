package io.effi.rpc.boot.builder;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.parameter.MethodMapper;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.util.AssertUtil;

/**
 * Builds {@link Callee} instance and defines configuration.
 */
public abstract class CalleeBuilder<T extends Callee, C extends CalleeBuilder<T, C>>
        extends CallSideBuilder<T, C> {

    protected MethodMapper<?> methodMapper;

    protected CalleeBuilder(MethodMapper<?> methodMapper, NodeConfig config) {
        super(config);
        this.methodMapper = AssertUtil.notNull(methodMapper, "methodMapper");
        this.container = methodMapper.remoteService();
    }

    public C desc(String desc) {
        config.set(DefaultConfigNames.CALLEE_DESC, desc);
        return returnThis();
    }

    public MethodMapper<?> methodMapper() {
        return methodMapper;
    }

}
