package io.effi.rpc.component;

/**
 * Provides access to a {@link EffiRpcModule} instance.
 */
public interface ModuleSource extends ApplicationSource {

    EffiRpcModule module();

    @Override
    default EffiRpcApplication application() {
        return module().application();
    }

    /**
     * Holds and provides an {@link EffiRpcModule} instance.
     */
    abstract class Holder implements ModuleSource {

        protected EffiRpcModule module;

        public Holder(EffiRpcModule module) {
            this.module = module;
        }

        @Override
        public EffiRpcModule module() {
            return module;
        }
    }
}
