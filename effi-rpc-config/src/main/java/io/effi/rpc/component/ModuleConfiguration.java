package io.effi.rpc.component;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.lifecycle.LifecycleConfiguration;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Manages the lifecycle of a {@link EffiRpcModule} instance.
 */
@Extensible(lazyLoad = false, scope = MODULE)
public interface ModuleConfiguration extends LifecycleConfiguration<EffiRpcModule> {}
