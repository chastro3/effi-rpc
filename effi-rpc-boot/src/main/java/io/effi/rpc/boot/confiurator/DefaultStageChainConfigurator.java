package io.effi.rpc.boot.confiurator;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.boot.confiurator.stage.CallInterceptStage;
import io.effi.rpc.boot.confiurator.stage.ChosenInterceptStage;
import io.effi.rpc.boot.confiurator.stage.FutureResultStage;
import io.effi.rpc.boot.confiurator.stage.InvokeServantStage;
import io.effi.rpc.boot.confiurator.stage.LocatorStage;
import io.effi.rpc.boot.confiurator.stage.ReplyInterceptStage;
import io.effi.rpc.boot.confiurator.stage.ReplyResultStage;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.context.ConfigurableServant;
import io.effi.rpc.context.ConfigurableCaller;
import io.effi.rpc.context.ConfigurablePeer;
import io.effi.rpc.context.Stage;
import io.effi.rpc.context.support.ImmutableStageChain;

import static io.effi.rpc.boot.confiurator.DefaultStageChainConfigurator.NAME;

@Extension(value = NAME, primary = true)
public class DefaultStageChainConfigurator implements ConfigurablePeer.StageChainConfigurator, ScopedModule.Acceptor {

    public static final String NAME = Constant.DEFAULT_NAME;

    private Stage.Chain defaultCallerCallChain;

    private Stage.Chain defaultServantCallChain;

    private Stage.Chain defaultCallerReplyChain;

    private Stage.Chain defaultServantReplyChain;

    @Override
    public void accept(ScopedModule module) {
        initializeDefaultStageChain(module);
    }

    @Override
    public void configure(ConfigurablePeer peer) {
        if (peer instanceof ConfigurableCaller<?> caller) {
            if (caller.callStageChain() == null) {
                caller.callStageChain(defaultCallerCallChain);
            }
            if (caller.replyStageChain() == null) {
                caller.replyStageChain(defaultCallerReplyChain);
            }
        } else if (peer instanceof ConfigurableServant servant) {
            if (servant.callStageChain() == null) {
                servant.callStageChain(defaultServantCallChain);
            }
            if (servant.replyStageChain() == null) {
                servant.replyStageChain(defaultServantReplyChain);
            }
        }
    }

    private void initializeDefaultStageChain(ScopedModule module) {
        String[] callerCallChainNames = {
                CallInterceptStage.NAME, LocatorStage.NAME, ChosenInterceptStage.NAME, FutureResultStage.NAME
        };
        String[] servantCallChainNames = new String[]{
                CallInterceptStage.NAME, InvokeServantStage.NAME
        };
        String[] replyChainNames = {
                ReplyInterceptStage.NAME, ReplyResultStage.NAME
        };
        defaultCallerCallChain = ImmutableStageChain.of(module, callerCallChainNames);
        defaultServantCallChain = ImmutableStageChain.of(module, servantCallChainNames);
        defaultCallerReplyChain = ImmutableStageChain.of(module, replyChainNames);
        defaultServantReplyChain = defaultCallerReplyChain;
    }
}
