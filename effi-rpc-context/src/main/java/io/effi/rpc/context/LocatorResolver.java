package io.effi.rpc.context;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.component.ScopedPlatform;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Resolves the locator used by a peer.
 */
@Extensible(scope = PLATFORM)
public interface LocatorResolver {

    Locator resolve(PeerDescriptor descriptor, ScopedPlatform platform);
}
