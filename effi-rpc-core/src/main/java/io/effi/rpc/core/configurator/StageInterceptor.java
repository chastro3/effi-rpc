package io.effi.rpc.core.configurator;

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

    /**
     * Creates an interceptor that delegates interception to the supplied stage chain.
     *
     * @param stageChain stage chain to execute
     * @return stage interceptor
     */
    public static StageInterceptor create(ImmutableStageChain stageChain) {
        AssertUtil.notNull(stageChain, "stageChain");
        return new StageInterceptor(stageChain);
    }

    @Override
    public Interaction.Result intercept(Interaction.Context<Message, Peer> context, Chain chain) {
        return stageChain.proceed(context);
    }

    /**
     * Returns the interceptor name derived from the wrapped stage chain.
     */
    public String name() {
        return findName();
    }

    private String findName() {
        return PREFIX + StringUtil.isBlankOrDefault(stageChain.name(), StringUtil.empty());
    }
}
