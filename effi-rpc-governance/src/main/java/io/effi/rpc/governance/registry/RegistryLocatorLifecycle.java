package io.effi.rpc.governance.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;

/**
 * Releases locator cache entries when their owning platform closes.
 */
@Extension("registryLocatorLifecycle")
public final class RegistryLocatorLifecycle implements ScopedPlatform.Listener {

    @Override
    public void onClosed(ScopedPlatform platform) {
        RegistryLocator.evict(platform);
    }
}
