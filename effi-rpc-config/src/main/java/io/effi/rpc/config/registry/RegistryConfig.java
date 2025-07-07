package io.effi.rpc.config.registry;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.TagComponent;
import io.effi.rpc.config.NamedURLConfig;
import io.effi.rpc.util.StringUtil;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;

/**
 * Defines configuration for registry.
 */
@ScopedComponent(scope = APPLICATION)
public interface RegistryConfig extends NamedURLConfig, TagComponent {

    default String type() {
        return url().protocol();
    }


    @Override
    default String id() {
        return StringUtil.isBlankOrDefault(name(), url().protocol());
    }



}
