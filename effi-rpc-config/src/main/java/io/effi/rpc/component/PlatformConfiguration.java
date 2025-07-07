package io.effi.rpc.component;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.lifecycle.LifecycleConfiguration;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Manages the lifecycle of a {@link EffiRpcPlatform} instance.
 */
@Extensible(lazyLoad = false, scope = PLATFORM)
public interface PlatformConfiguration extends LifecycleConfiguration<EffiRpcPlatform> {}