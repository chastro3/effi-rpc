package io.effi.rpc.base.context;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.Result;
import io.effi.rpc.util.StringUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Executes the next {@link Stage} in the {@link ImmutableStageChain}.
 *
 * <p>This interceptor wraps a {@link StageChain} and delegates interception to it.
 * It is typically used within a {@link Stage} to invoke subsequent stages in the chain.</p>
 *
 * <p>Instances are cached and identified by names prefixed with "{@value #PREFIX}".</p>
 *
 * @param <C> the type of the exchange context extending {@link ExchangeContext} with {@link Message} and {@link CallSide}
 */
public final class StageInterceptor<C extends ExchangeContext<Message, CallSide>> implements Interceptor<Message, CallSide, C> {

    public static final String PREFIX = "stage$";

    private static final Map<String, StageInterceptor<?>> CACHE = new ConcurrentHashMap<>();

    private final ImmutableStageChain stageChain;

    private StageInterceptor(ImmutableStageChain stageChain) {
        this.stageChain = stageChain;
    }

    public static StageInterceptor<?> getInstance(String name) {
        return CACHE.get(name);
    }

    public static StageInterceptor<?> getInstance(ImmutableStageChain stageChain) {
        if (stageChain == null) return null;
        return CACHE.computeIfAbsent(getName(stageChain), k -> new StageInterceptor<>(stageChain));
    }

    private static String getName(ImmutableStageChain stageChain) {
        return PREFIX + StringUtil.isBlankOrDefault(stageChain.name(), StringUtil.empty());
    }

    @Override
    public Result intercept(C context, InterceptorChain chain) {
        return stageChain.proceed(context);
    }

    public String name() {
        return getName(stageChain);
    }
}
