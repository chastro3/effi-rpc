package io.effi.rpc.config;

import io.effi.rpc.annotation.component.Extensible;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Maps custom protocol names to standard protocol names.
 */
@Extensible(lazyLoad = false, scope = PLATFORM)
public interface URLProtocolMapper {

    /**
     * Returns the supported protocol name.
     */
    String supported();

    /**
     * Returns the mapped protocol name.
     */
    String mapped();
}

