package io.effi.rpc.component;

import io.effi.rpc.annotation.component.ScopedComponent;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Manages module-level resources and lifecycle.
 */
@ScopedComponent(scope = APPLICATION)
public final class EffiRpcModule extends AbstractScopedComponentRepository implements ApplicationSource {

    EffiRpcModule(String name, EffiRpcApplication parent) {
        name(name);
        initialize(MODULE, parent, ModuleConfiguration.class);
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
}
