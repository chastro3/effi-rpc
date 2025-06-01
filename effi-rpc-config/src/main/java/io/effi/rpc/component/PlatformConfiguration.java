package io.effi.rpc.component;

import io.effi.rpc.annotation.spi.Extensible;
import io.effi.rpc.lifecycle.LifecycleConfiguration;

/**
 * Manages the lifecycle of a {@link EffiRpcPlatform} instance.
 */
@Extensible(lazyLoad = false)
public interface PlatformConfiguration extends LifecycleConfiguration<EffiRpcPlatform> {}