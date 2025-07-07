package io.effi.rpc.boot.util;

import io.effi.rpc.base.context.Interceptor;
import io.effi.rpc.base.context.ReplyInterceptor;
import io.effi.rpc.base.context.UnitType;

import java.util.function.BiConsumer;

/**
 * Provides a reusable {@link ExecutionUnitClassifier.Handler} for classifying {@link ReplyInterceptor}s.
 *
 * <p>Matches {@link Interceptor} instances that are {@link ReplyInterceptor}
 * and support the protocol's response type.</p>
 */
@SuppressWarnings("rawtypes")
public class ReplyInterceptorClassifyHandler {

    private static final ReplyInterceptorRule RULE = new ReplyInterceptorRule();

    public static ExecutionUnitClassifier.Handler<Interceptor> of(BiConsumer<String, Interceptor> consumer) {
        return ExecutionUnitClassifier.Handler.of(RULE, consumer);
    }

    static class ReplyInterceptorRule implements ExecutionUnitClassifier.Rule<Interceptor> {
        @Override
        public boolean test(String name, Interceptor unit, UnitType<?, ?> unitType, ExecutionUnitClassifier<Interceptor> classifier) {
            return unit instanceof ReplyInterceptor && classifier.supportResponse(unitType);
        }
    }
}
