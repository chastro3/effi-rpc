package io.effi.rpc.context;

import io.effi.rpc.util.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Executes stages in the stage chain as interceptors.
 * <p>
 * Provides interceptor functionality that wraps {@link Stage.Chain} and delegates
 * interception to subsequent {@link Stage} with caching support for named instances.
 */
public final class StageInterceptor implements Interceptor<Message, Peer, Interaction.Context<Message, Peer>> {

    public static final String PREFIX = "stage$";

    private static final Map<String, StageInterceptor> CACHE = new ConcurrentHashMap<>();

    private final ImmutableStageChain stageChain;

    private StageInterceptor(ImmutableStageChain stageChain) {
        this.stageChain = stageChain;
    }

    public static StageInterceptor lookup(String name) {
        return CACHE.get(name);
    }

    public static StageInterceptor lookup(ImmutableStageChain stageChain) {
        if (stageChain == null) return null;
        return CACHE.computeIfAbsent(findName(stageChain), k -> new StageInterceptor(stageChain));
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
