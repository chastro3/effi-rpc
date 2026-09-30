package io.effi.rpc.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ScopedPlatformLifecycleTest {

    @Test
    void closedPlatformIsRemovedFromLookup() {
        String name = "platform-lifecycle-test";
        ScopedPlatform platform = new ScopedPlatform(name);

        assertSame(platform, ScopedPlatform.lookup(name));
        platform.close();
        assertNull(ScopedPlatform.lookup(name));
    }
}
