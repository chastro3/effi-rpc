package io.effi.rpc.component;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.lifecycle.LifecycleConfiguration;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;

/**
 * Manages the lifecycle of a {@link EffiRpcApplication} instance.
 */
@Extensible(lazyLoad = false, scope = APPLICATION)
public interface ApplicationConfiguration extends LifecycleConfiguration<EffiRpcApplication> {}

