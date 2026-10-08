package io.effi.rpc.core.configurator.classifier;

import io.effi.rpc.context.Stage;
import io.effi.rpc.context.UnitType;

import java.util.function.BiConsumer;

/**
 * Provides a reusable classifier for reply stages.
 */
@SuppressWarnings("rawtypes")
public class ReplyStageClassifier {

    private static final ReplyStageRule RULE = new ReplyStageRule();

    private ReplyStageClassifier() {
    }

    /**
     * Creates a handler that collects reply stages.
     *
     * @param consumer callback receiving the extension name and reply stage
     * @return classifier handler
     */
    public static InteractionUnitClassifier.Handler<Stage> handler(BiConsumer<String, Stage> consumer) {
        return InteractionUnitClassifier.Handler.of(RULE, consumer);
    }

    static class ReplyStageRule implements InteractionUnitClassifier.Rule<Stage> {

        @Override
        public boolean test(String name, Stage unit, UnitType<?, ?> unitType, InteractionUnitClassifier<Stage> classifier) {
            return unit instanceof Stage.ReplyUnit && classifier.supportResponse(unitType);
        }
    }
}
