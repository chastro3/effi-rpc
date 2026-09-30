package io.effi.rpc.component.extension;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedPlatform;

@Extension("recursive")
public final class RecursiveExtensionImpl implements RecursiveExtension {

    static ScopedPlatform context;

    public RecursiveExtensionImpl() {
        context.extensionLoader(RecursiveExtension.class);
    }
}
