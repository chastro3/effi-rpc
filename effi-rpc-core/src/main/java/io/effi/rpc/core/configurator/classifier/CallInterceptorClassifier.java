package io.effi.rpc.core.configurator.classifier;

import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.UnitType;

import java.util.function.BiConsumer;

/**
 * Provides a reusable classifier for call interceptors.
 */
@SuppressWarnings("rawtypes")
public class CallInterceptorClassifier {

    private static final CallInterceptorRule RULE = new CallInterceptorRule();

    private CallInterceptorClassifier() {
    }

    /**
     * Creates a handler that collects call interceptors.
     *
     * @param consumer callback receiving the extension name and call interceptor
     * @return classifier handler
     */
    public static InteractionUnitClassifier.Handler<Interceptor> handler(BiConsumer<String, Interceptor> consumer) {
        return InteractionUnitClassifier.Handler.of(RULE, consumer);
    }

    static class CallInterceptorRule implements InteractionUnitClassifier.Rule<Interceptor> {
        @Override
        public boolean test(String name, Interceptor unit, UnitType<?, ?> unitType, InteractionUnitClassifier<Interceptor> classifier) {
            return unit instanceof Interceptor.CallUnit && classifier.supportRequest(unitType);
        }
    }
}
