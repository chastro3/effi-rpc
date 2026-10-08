package io.effi.rpc.core.configurator.classifier;

import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.UnitType;

import java.util.function.BiConsumer;

/**
 * Provides a reusable classifier for reply interceptors.
 */
@SuppressWarnings("rawtypes")
public class ReplyInterceptorClassifier {

    private static final ReplyInterceptorRule RULE = new ReplyInterceptorRule();

    private ReplyInterceptorClassifier() {
    }

    /**
     * Creates a handler that collects reply interceptors.
     *
     * @param consumer callback receiving the extension name and reply interceptor
     * @return classifier handler
     */
    public static InteractionUnitClassifier.Handler<Interceptor> handler(BiConsumer<String, Interceptor> consumer) {
        return InteractionUnitClassifier.Handler.of(RULE, consumer);
    }

    static class ReplyInterceptorRule implements InteractionUnitClassifier.Rule<Interceptor> {

        @Override
        public boolean test(String name, Interceptor unit, UnitType<?, ?> unitType, InteractionUnitClassifier<Interceptor> classifier) {
            return unit instanceof Interceptor.ReplyUnit && classifier.supportResponse(unitType);
        }
    }
}
