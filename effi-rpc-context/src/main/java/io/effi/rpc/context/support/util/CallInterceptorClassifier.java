package io.effi.rpc.context.support.util;

import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.UnitType;

import java.util.function.BiConsumer;

/**
 * Provides a reusable {@link InteractionUnitClassifier.Handler} for classifying {@link CallInterceptor}s.
 *
 * <p>Matches {@link Interceptor} instances that are {@link CallInterceptor} and support the protocol's request type.</p>
 */
@SuppressWarnings("rawtypes")
public class CallInterceptorClassifier {

    private static final CallInterceptorRule RULE = new CallInterceptorRule();

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
