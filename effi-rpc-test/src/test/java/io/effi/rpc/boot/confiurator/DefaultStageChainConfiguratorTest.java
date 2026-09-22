package io.effi.rpc.boot.confiurator;

import io.effi.rpc.boot.confiurator.stage.CallInterceptStage;
import io.effi.rpc.boot.confiurator.stage.ChosenInterceptStage;
import io.effi.rpc.boot.confiurator.stage.FutureResultStage;
import io.effi.rpc.boot.confiurator.stage.LocatorStage;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.ConfigurablePeer;
import io.effi.rpc.context.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultStageChainConfiguratorTest {

    private static final String[] CALLER_CALL_STAGES = {
            CallInterceptStage.NAME, LocatorStage.NAME, ChosenInterceptStage.NAME, FutureResultStage.NAME
    };

    @Test
    void stageChainCacheIsIsolatedByModule() {
        ScopedPlatform platform = new ScopedPlatform("stage-chain-isolation-platform");
        ScopedModule firstModule = platform.newApplication("first-application")
                .newModule("first-module");
        ScopedModule secondModule = platform.newApplication("second-application")
                .newModule("second-module");

        DefaultStageChainConfigurator firstConfigurator = configurator(firstModule);
        DefaultStageChainConfigurator secondConfigurator = configurator(secondModule);

        Stage.Chain firstChain = firstConfigurator.resolveStageChain(CALLER_CALL_STAGES);
        Stage.Chain repeatedFirstChain = firstConfigurator.resolveStageChain(CALLER_CALL_STAGES);
        Stage.Chain secondChain = secondConfigurator.resolveStageChain(CALLER_CALL_STAGES);

        assertSame(firstChain, repeatedFirstChain);
        assertNotSame(firstChain, secondChain);
    }

    private DefaultStageChainConfigurator configurator(ScopedModule module) {
        return (DefaultStageChainConfigurator) module.namedExtension(
                ConfigurablePeer.StageChainConfigurator.class,
                Constant.DEFAULT_NAME
        );
    }
}
