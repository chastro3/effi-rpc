package io.effi.rpc.config.registry;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.PlatformSource;
import io.effi.rpc.config.NamedURLConfig;
import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines configuration for registry.
 */
@ScopedComponent(scope = PLATFORM)
public interface RegistryConfig extends NamedURLConfig, PlatformSource {

    default String type() {
        return url().protocol();
    }


    @Override
    default String id() {
        return StringUtil.isBlankOrDefault(name(), url().protocol());
    }



}
