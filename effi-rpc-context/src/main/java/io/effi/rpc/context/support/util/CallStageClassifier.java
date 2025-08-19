package io.effi.rpc.context.support.util;

import io.effi.rpc.context.Stage;
import io.effi.rpc.context.UnitType;

import java.util.function.BiConsumer;

@SuppressWarnings("rawtypes")
public class CallStageClassifier {

    private static final CallStageRule RULE = new CallStageRule();

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
