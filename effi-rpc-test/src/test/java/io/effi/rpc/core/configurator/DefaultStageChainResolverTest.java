package io.effi.rpc.core.configurator;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.Stage;
import io.effi.rpc.core.stage.CallAttemptStage;
import io.effi.rpc.core.stage.CallInterceptorStage;
import io.effi.rpc.core.stage.ChosenInterceptorStage;
import io.effi.rpc.core.stage.LocatorStage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class DefaultStageChainResolverTest {

    private static final String[] CALLER_CALL_STAGES = {
            CallInterceptorStage.NAME, LocatorStage.NAME, ChosenInterceptorStage.NAME, CallAttemptStage.NAME
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
