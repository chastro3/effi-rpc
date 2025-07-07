package io.effi.rpc.boot.util;

import io.effi.rpc.base.context.ChosenInterceptor;
import io.effi.rpc.base.context.Interceptor;
import io.effi.rpc.base.context.UnitType;

import java.util.function.BiConsumer;

/**
 * Provides a reusable {@link ExecutionUnitClassifier.Handler} for classifying {@link ChosenInterceptor}s.
 *
 * <p>Matches {@link Interceptor} instances that are {@link ChosenInterceptor} and support the protocol's request type.</p>
 */
@SuppressWarnings("rawtypes")
public class ChosenInterceptorClassifyHandler {

    private static final ChosenInterceptorRule RULE = new ChosenInterceptorRule();

    public static ExecutionUnitClassifier.Handler<Interceptor> of(BiConsumer<String, Interceptor> consumer) {
        return ExecutionUnitClassifier.Handler.of(RULE, consumer);
    }

    static class ChosenInterceptorRule implements ExecutionUnitClassifier.Rule<Interceptor> {
        @Override
        public boolean test(String name, Interceptor unit, UnitType<?, ?> unitType, ExecutionUnitClassifier<Interceptor> classifier) {
            return unit instanceof ChosenInterceptor && classifier.supportRequest(unitType);
        }
    }
}
