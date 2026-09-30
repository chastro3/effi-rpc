package io.effi.rpc.component.extension;

import io.effi.rpc.component.ScopedPlatform;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExtensionRepositoryTest {

    @Test
    void registersLoaderBeforeEagerExtensionInitialization() {
        ScopedPlatform platform = new ScopedPlatform("recursive-extension-test");
        RecursiveExtensionImpl.context = platform;
        try {
            assertNotNull(platform.namedExtension(RecursiveExtension.class, "recursive"));
        } finally {
            RecursiveExtensionImpl.context = null;
            platform.close();
        }
    }
}
