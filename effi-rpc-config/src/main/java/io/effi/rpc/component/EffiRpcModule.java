package io.effi.rpc.component;

import io.effi.rpc.annotation.component.ScopedComponent;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Manages module-level resources and lifecycle.
 */
@ScopedComponent(scope = APPLICATION)
public final class EffiRpcModule extends ScopedContext implements EffiRpcApplication.Provider {

    EffiRpcModule(String name, EffiRpcApplication parent) {
        this(name, parent, null);
    }

    EffiRpcModule(String name, EffiRpcApplication parent, ComponentStore repository) {
        name(name);
        initialize(MODULE, parent, repository, ModuleConfiguration.class);
    }

    @Override
    public EffiRpcApplication application() {
        return (EffiRpcApplication) parent();
    }

    @Override
    protected void doStart() {

    }

    @Override
    protected void doStop() {

    }

    /**
     * Provides access to the {@link EffiRpcModule}.
     */
    public interface Provider extends EffiRpcApplication.Provider {

        @Override
        default EffiRpcApplication application() {
            return module().application();
        }

        /**
         * Returns the associated {@link EffiRpcModule}.
         */
        EffiRpcModule module();
    }

    /**
     * Holds a reference to an {@link EffiRpcModule}.
     */
    public abstract static class Holder implements Provider {

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
