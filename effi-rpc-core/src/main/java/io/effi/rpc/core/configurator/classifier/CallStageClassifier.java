package io.effi.rpc.core.configurator.classifier;

import io.effi.rpc.context.Stage;
import io.effi.rpc.context.UnitType;

import java.util.function.BiConsumer;

/**
 * Provides a reusable classifier for call stages.
 */
@SuppressWarnings("rawtypes")
public class CallStageClassifier {

    private static final CallStageRule RULE = new CallStageRule();

    private CallStageClassifier() {
    }

    /**
     * Creates a handler that collects call stages.
     *
     * @param consumer callback receiving the extension name and call stage
     * @return classifier handler
     */
    public static InteractionUnitClassifier.Handler<Stage> handler(BiConsumer<String, Stage> consumer) {
        return InteractionUnitClassifier.Handler.of(RULE, consumer);
    }

    static class CallStageRule implements InteractionUnitClassifier.Rule<Stage> {
        @Override
        public boolean test(String name, Stage unit, UnitType<?, ?> unitType, InteractionUnitClassifier<Stage> classifier) {
            return unit instanceof Stage.CallUnit && classifier.supportRequest(unitType);
        }
    }
}
