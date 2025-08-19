package io.effi.rpc.context.support.util;

import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Stage;
import io.effi.rpc.context.UnitType;

import java.util.function.BiConsumer;

/**
 * Provides a reusable {@link InteractionUnitClassifier.Handler} for classifying {@link ReplyInterceptor}s.
 *
 * <p>Matches {@link Interceptor} instances that are {@link ReplyInterceptor}
 * and support the protocol's response type.</p>
 */
@SuppressWarnings("rawtypes")
public class ReplyStageClassifier {

    private static final ReplyStageRule RULE = new ReplyStageRule();

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
