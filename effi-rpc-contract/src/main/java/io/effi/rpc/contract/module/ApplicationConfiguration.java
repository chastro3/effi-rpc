package io.effi.rpc.contract.module;

import io.effi.rpc.util.LifecycleConfiguration;
import io.effi.rpc.spi.Extensible;

/**
 * Manages the lifecycle of a {@link EffRpcApplication} instance.
 */
@Extensible(lazyLoad = false)
public interface ApplicationConfiguration extends LifecycleConfiguration<EffRpcApplication> {}

