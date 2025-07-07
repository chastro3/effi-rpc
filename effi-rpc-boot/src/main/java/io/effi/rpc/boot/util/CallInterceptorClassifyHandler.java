package io.effi.rpc.boot.util;

import io.effi.rpc.base.context.CallInterceptor;
import io.effi.rpc.base.context.Interceptor;
import io.effi.rpc.base.context.UnitType;

import java.util.function.BiConsumer;

/**
 * Provides a reusable {@link ExecutionUnitClassifier.Handler} for classifying {@link CallInterceptor}s.
 *
 * <p>Matches {@link Interceptor} instances that are {@link CallInterceptor} and support the protocol's request type.</p>
 */
@SuppressWarnings("rawtypes")
public class CallInterceptorClassifyHandler {

    private static final CallInterceptorRule RULE = new CallInterceptorRule();

    public static ExecutionUnitClassifier.Handler<Interceptor> of(BiConsumer<String, Interceptor> consumer) {
        return ExecutionUnitClassifier.Handler.of(RULE, consumer);
    }

    static class CallInterceptorRule implements ExecutionUnitClassifier.Rule<Interceptor> {
        @Override
        public boolean test(String name, Interceptor unit, UnitType<?, ?> unitType, ExecutionUnitClassifier<Interceptor> classifier) {
            return unit instanceof CallInterceptor && classifier.supportRequest(unitType);
        }
    }
}
