package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.option.Options;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Resolves the locator used by a peer.
 */
@Extensible(scope = PLATFORM)
public interface LocatorResolver {

    /**
     * Resolves the locator configured by the supplied options.
     *
     * @param options  peer options
     * @param platform owning platform
     * @return resolved locator
     */
    Locator resolve(Options options, ScopedPlatform platform);
}
