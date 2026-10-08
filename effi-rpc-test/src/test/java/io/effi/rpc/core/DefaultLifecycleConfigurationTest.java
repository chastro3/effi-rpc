package io.effi.rpc.core;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.event.EventBus;
import io.effi.rpc.component.event.MpscEventBus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultLifecycleConfigurationTest {

    @Test
    void registersAndClosesEventBus() {
        ScopedPlatform platform = new ScopedPlatform(
                "event-bus-lifecycle-" + System.nanoTime()
        );
        EventBus eventBus;
        try {
            eventBus = platform.singleComponent(EventBus.class);
            assertInstanceOf(MpscEventBus.class, eventBus);
            assertTrue(eventBus.active());
        } finally {
            platform.close();
        }
        assertFalse(eventBus.active());
    }
}
