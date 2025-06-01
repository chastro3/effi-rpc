package io.effi.rpc.component;

import io.effi.rpc.annotation.spi.Extensible;
import io.effi.rpc.lifecycle.LifecycleConfiguration;

/**
 * Manages the lifecycle of a {@link EffiRpcModule} instance.
 */
@Extensible(lazyLoad = false)
public interface ModuleConfiguration extends LifecycleConfiguration<EffiRpcModule> {}
