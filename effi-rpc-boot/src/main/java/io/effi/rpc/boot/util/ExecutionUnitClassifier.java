package io.effi.rpc.boot.util;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.context.ExecutionUnit;
import io.effi.rpc.base.context.UnitType;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Filters and classifies {@link ExecutionUnit} instances for a given {@link CallSide} and {@link Protocol}.
 *
 * <p>Applies custom {@link Handler} rules to determine which units should be consumed and added
 * to corresponding execution chains.</p>
 *
 * <p>This classifier helps build stage or interceptor chains dynamically based on unit types
 * and supported protocol message types.</p>
 *
 * <p>Example usage:
 * <pre>{@code
 *   new ExecutionUnitClassifier<>(callSide, protocol)
 *       .handler(Handler.of(myRule, (name, unit) -> addToChain(name, unit)))
 *       .classify(allUnits);
 * }</pre>
 * </p>
 *
 * @param <T> the execution unit type
 */
@SuppressWarnings("rawtypes")
public class ExecutionUnitClassifier<T extends ExecutionUnit> {

    private final CallSide callSide;

    private final Protocol protocol;

    private final List<Handler<T>> handlers = new ArrayList<>(4);

    public ExecutionUnitClassifier(CallSide callSide, Protocol protocol) {
        this.callSide = AssertUtil.notNull(callSide, "callSide");
        this.protocol = AssertUtil.notNull(protocol, "protocol");
    }


    public ExecutionUnitClassifier<T> handler(Handler<T> handler) {
        handlers.add(handler);
        return this;
    }

    public void classify(Map<String, T> eus) {
        if (CollectionUtil.isNotEmpty(eus) && CollectionUtil.isNotEmpty(handlers)) {
            for (Map.Entry<String, T> entry : eus.entrySet()) {
                String key = entry.getKey();
                T value = entry.getValue();
                UnitType<?, ?> type = UnitType.extract(value);
                Class<?> sideType = type.callSideType();
                for (Handler<T> handler : handlers) {
                    if (sideType.isAssignableFrom(callSide.getClass())
                            && handler.predicate.test(key, value, type, this)) {
                        handler.consumer.accept(key, value);
                    }
                }
            }
        }
    }

    public boolean supportResponse(UnitType<?, ?> type) {
        return type.messageType().isAssignableFrom(protocol.supportedResponseType());
    }

    public boolean supportRequest(UnitType<?, ?> type) {
        return type.messageType().isAssignableFrom(protocol.supportedRequestType());
    }


    /**
     * Defines a predicate rule to test whether an {@link ExecutionUnit}
     * matches certain conditions for the current {@link CallSide} and {@link Protocol}.
     *
     * @param <T> the execution unit type
     */
    @FunctionalInterface
    public interface Rule<T extends ExecutionUnit> {
        boolean test(String name, T unit, UnitType<?, ?> unitType, ExecutionUnitClassifier<T> classifier);
    }

    /**
     * Defines a handler that applies a {@link Rule} to filter units
     * and consumes matching {@link ExecutionUnit}s.
     *
     * @param <T> the execution unit type
     */
    public static final class Handler<T extends ExecutionUnit> {
        public final Rule<T> predicate;
        public final BiConsumer<String, T> consumer;

        private Handler(Rule<T> predicate, BiConsumer<String, T> consumer) {
            this.predicate = predicate;
            this.consumer = consumer;
        }

        public static <T extends ExecutionUnit> Handler<T> of(Rule<T> predicate, BiConsumer<String, T> consumer) {
            return new Handler<>(predicate, consumer);
        }
    }
}
