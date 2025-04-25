package io.effi.rpc.contract.module;

import io.effi.rpc.util.LifecycleConfiguration;
import io.effi.rpc.spi.Extensible;

/**
 * Manages the lifecycle of a {@link EffiRpcModule} object.
 */
@Extensible(lazyLoad = false)
public interface ModuleConfiguration extends LifecycleConfiguration<EffiRpcModule> {}
