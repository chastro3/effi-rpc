package io.effi.rpc.boot.confiurator;

import io.effi.rpc.boot.confiurator.stage.CallInterceptStage;
import io.effi.rpc.boot.confiurator.stage.ChosenInterceptStage;
import io.effi.rpc.boot.confiurator.stage.FutureResultStage;
import io.effi.rpc.boot.confiurator.stage.LocatorStage;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.Stage;
import io.effi.rpc.context.StageChainResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultStageChainResolverTest {

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

        DefaultStageChainResolver firstResolver = resolver(firstModule);
        DefaultStageChainResolver secondResolver = resolver(secondModule);

        Stage.Chain firstChain = firstResolver.resolveStageChain(firstModule, CALLER_CALL_STAGES);
        Stage.Chain repeatedFirstChain = firstResolver.resolveStageChain(firstModule, CALLER_CALL_STAGES);
        Stage.Chain secondChain = secondResolver.resolveStageChain(secondModule, CALLER_CALL_STAGES);

        assertSame(firstChain, repeatedFirstChain);
        assertNotSame(firstChain, secondChain);
    }

    private DefaultStageChainResolver resolver(ScopedModule module) {
        return (DefaultStageChainResolver) module.namedExtension(
                StageChainResolver.class,
                Constant.DEFAULT_NAME
        );
    }
}
