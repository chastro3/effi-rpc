package io.effi.rpc.base.context;

import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringArrayKey;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an immutable chain of {@link Stage} instances.
 *
 * <p>Supports efficient caching and creation of stage chains by name arrays or maps.</p>
 *
 * @see ImmutableExecutionUnitChain
 * @see StageChain
 */
@SuppressWarnings("rawtypes")
public final class ImmutableStageChain extends ImmutableExecutionUnitChain<Stage, ImmutableStageChain> implements StageChain {

    private static final ImmutableStageChain EMPTY_CHAIN = new ImmutableStageChain("empty", null, null);

    private static final Map<StringArrayKey, ImmutableStageChain> CACHE = new ConcurrentHashMap<>();

    public static ImmutableStageChain of(EffiRpcModule module, String[] names) {
        if (CollectionUtil.isEmpty(names)) return EMPTY_CHAIN;
        StringArrayKey key = StringArrayKey.of(names);
        return CACHE.computeIfAbsent(key, k ->
                init(module, ImmutableStageChain::getStage, names, ImmutableStageChain::new, EMPTY_CHAIN)
        );
    }

    private static Stage getStage(EffiRpcModule module, String name) {
        return module.getExtension(Stage.class, name);
    }

    private ImmutableStageChain(String name, Stage stage, ImmutableStageChain next) {
        super(name, stage, next);
    }

    public static ImmutableStageChain init(Map<String, Stage> stages) {
        return init(stages, ImmutableStageChain::new, EMPTY_CHAIN);
    }

}
