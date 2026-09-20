package io.effi.rpc.test;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.ConfigurablePeer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;

class ExtensionContextIsolationTest {

    @Test
    void singletonExtensionIsIsolatedByModule() {
        ScopedPlatform platform = new ScopedPlatform("extension-isolation-platform");
        ScopedModule firstModule = platform.newApplication("first-application")
                .newModule("first-module");
        ScopedModule secondModule = platform.newApplication("second-application")
                .newModule("second-module");

        ConfigurablePeer.StageChainConfigurator first = firstModule.namedExtension(
                ConfigurablePeer.StageChainConfigurator.class,
                Constant.DEFAULT_NAME
        );
        ConfigurablePeer.StageChainConfigurator second = secondModule.namedExtension(
                ConfigurablePeer.StageChainConfigurator.class,
                Constant.DEFAULT_NAME
        );

        assertNotSame(first, second);
    }
}
