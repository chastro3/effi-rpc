package io.effi.rpc.context.support;

import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Message;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Stage;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.StringUtil;

/**
 * Executes stages in the stage chain as interceptors.
 * <p>
 * Provides interceptor functionality that wraps {@link Stage.Chain} and delegates
 * interception to subsequent {@link Stage}.
 */
public final class StageInterceptor implements Interceptor<Message, Peer, Interaction.Context<Message, Peer>> {

    public static final String PREFIX = "stage$";

    private final ImmutableStageChain stageChain;

    private StageInterceptor(ImmutableStageChain stageChain) {
        this.stageChain = stageChain;
    }

    public static StageInterceptor create(ImmutableStageChain stageChain) {
        AssertUtil.notNull(stageChain, "stageChain");
        return new StageInterceptor(stageChain);
    }

    @Override
    public Interaction.Result intercept(Interaction.Context<Message, Peer> context, Chain chain) {
        return stageChain.proceed(context);
    }

    public String name() {
        return findName(stageChain);
    }

    private static String findName(ImmutableStageChain stageChain) {
        return PREFIX + StringUtil.isBlankOrDefault(stageChain.name(), StringUtil.empty());
    }
}
