package io.effi.rpc.core.configurator;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Stage;
import io.effi.rpc.util.CollectionUtil;

import java.util.Map;

/**
 * Provides an immutable chain of stage instances.
 * <p>
 * Implements an immutable chain structure for processing stages with
 * creation support for stage chains.
 *
 * @see ImmutableInteractionUnitChain
 * @see Stage.Chain
 */
@SuppressWarnings("rawtypes")
public final class ImmutableStageChain extends ImmutableInteractionUnitChain<Stage, ImmutableStageChain> implements Stage.Chain {

    private static final Stage TAIL_STAGE = (ctx, chain) -> null;

    private static final ImmutableStageChain TAIL = new ImmutableStageChain("empty", TAIL_STAGE, null);

    private ImmutableStageChain(String name, Stage stage, ImmutableStageChain next) {
        super(name, stage, next);
    }

    /**
     * Creates a stage chain for the given module and stage names.
     *
     * @param module the scoped module to lookup stages from
     * @param names  the stage names to include in the chain
     * @return the stage chain, or empty chain if names are empty
     */
    public static ImmutableStageChain of(ScopedModule module, String[] names) {
        if (CollectionUtil.isEmpty(names)) return TAIL;
        return init(module, ImmutableStageChain::lookupStage, names, ImmutableStageChain::new, TAIL);
    }

    /**
     * Initializes a stage chain from the given stage map.
     *
     * @param stages the map of stages to include in the chain
     * @return the initialized stage chain
     */
    public static ImmutableStageChain init(Map<String, Stage> stages) {
        return init(stages, ImmutableStageChain::new, TAIL);
    }

    private static Stage lookupStage(ScopedModule module, String name) {
        return module.namedExtension(Stage.class, name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <C extends Interaction.Context> Interaction.Result proceed(C context) {
        return unit.process(context, next);
    }
}
