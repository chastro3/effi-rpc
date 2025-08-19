package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Creates locators for resolving target addresses in RPC calls.
 * <p>
 * Provides a factory interface for creating locator instances
 * based on target strings and caller configurations.
 */
@Extensible(scope = PLATFORM)
public interface LocatorFactory {

    /**
     * Fetches a locator for the specified target and caller.
     *
     * @param target the target address or identifier
     * @param caller the caller instance
     * @return the locator for resolving the target address
     */
    Locator fetch(String target, Caller<?> caller);
}
