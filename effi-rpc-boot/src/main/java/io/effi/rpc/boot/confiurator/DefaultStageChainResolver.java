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
import io.effi.rpc.context.PeerDescriptor;
import io.effi.rpc.context.Stage;
import io.effi.rpc.context.StageChainResolver;
import io.effi.rpc.context.support.ImmutableStageChain;
import io.effi.rpc.util.ArrayIdentifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.effi.rpc.boot.confiurator.DefaultStageChainResolver.NAME;

/**
 * Resolves the default stage chains for a peer.
 */
@Extension(value = NAME, primary = true)
public class DefaultStageChainResolver implements StageChainResolver, ScopedModule.Acceptor {

    public static final String NAME = Constant.DEFAULT_NAME;

    private final Map<ArrayIdentifier<String>, Stage.Chain> stageChainCache = new ConcurrentHashMap<>();

    private Stage.Chain defaultCallerCallChain;

    private Stage.Chain defaultServantCallChain;

    private Stage.Chain defaultReplyChain;

    @Override
    public void accept(ScopedModule module) {
        String[] callerCallChainNames = {
                CallInterceptStage.NAME, LocatorStage.NAME, ChosenInterceptStage.NAME, FutureResultStage.NAME
        };
        String[] servantCallChainNames = {
                CallInterceptStage.NAME, InvokeServantStage.NAME
        };
        String[] replyChainNames = {
                ReplyInterceptStage.NAME, ReplyResultStage.NAME
        };
        defaultCallerCallChain = resolveStageChain(module, callerCallChainNames);
        defaultServantCallChain = resolveStageChain(module, servantCallChainNames);
        defaultReplyChain = resolveStageChain(module, replyChainNames);
    }

    @Override
    public Stage.Chain resolveCallChain(PeerDescriptor descriptor, ScopedModule module) {
        return descriptor.kind() == PeerDescriptor.Kind.CALLER
                ? defaultCallerCallChain
                : defaultServantCallChain;
    }

    @Override
    public Stage.Chain resolveReplyChain(PeerDescriptor descriptor, ScopedModule module) {
        return defaultReplyChain;
    }

    Stage.Chain resolveStageChain(ScopedModule module, String[] names) {
        String[] stageNames = names.clone();
        ArrayIdentifier<String> key = ArrayIdentifier.of(stageNames);
        return stageChainCache.computeIfAbsent(key, ignored -> ImmutableStageChain.of(module, stageNames));
    }
}
