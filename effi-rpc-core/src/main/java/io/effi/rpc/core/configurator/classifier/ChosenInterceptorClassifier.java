package io.effi.rpc.core.configurator.classifier;

import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.UnitType;

import java.util.function.BiConsumer;

/**
 * Provides a reusable classifier for chosen interceptors.
 */
@SuppressWarnings("rawtypes")
public class ChosenInterceptorClassifier {

    private static final ChosenInterceptorRule RULE = new ChosenInterceptorRule();

    private ChosenInterceptorClassifier() {
    }

    /**
     * Creates a handler that collects chosen interceptors.
     *
     * @param consumer callback receiving the extension name and chosen interceptor
     * @return classifier handler
     */
    public static InteractionUnitClassifier.Handler<Interceptor> handler(BiConsumer<String, Interceptor> consumer) {
        return InteractionUnitClassifier.Handler.of(RULE, consumer);
    }

    static class ChosenInterceptorRule implements InteractionUnitClassifier.Rule<Interceptor> {
        @Override
        public boolean test(String name, Interceptor unit, UnitType<?, ?> unitType, InteractionUnitClassifier<Interceptor> classifier) {
            return unit instanceof Interceptor.ChosenUnit && classifier.supportRequest(unitType);
        }
    }
}
