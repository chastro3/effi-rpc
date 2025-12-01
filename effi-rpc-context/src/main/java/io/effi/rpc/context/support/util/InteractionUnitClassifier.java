package io.effi.rpc.context.support.util;

import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Protocol;
import io.effi.rpc.context.UnitType;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Filters and classifies {@link Executable.Unit} instances for a given {@link Peer} and {@link Protocol}.
 * <p>
 * Applies custom {@link Handler} rules to determine which units should be consumed and added
 * to corresponding execution chains.
 * <p>
 * This classifier helps build stage or interceptor chains dynamically based on unit types
 * and supported protocol message types.
 *
 * <p>Example usage:
 * <pre>{@code
 *   new ExecutionUnitClassifier<>(peer, protocol)
 *       .handler(Handler.of(myRule, (id, unit) -> addToChain(id, unit)))
 *       .classify(allUnits);
 * }</pre>
 * </p>
 *
 * @param <T> the execution unit type
 */
@SuppressWarnings("rawtypes")
public class InteractionUnitClassifier<T extends Interaction.Unit> {

    private final Peer peer;

    private final List<Handler<T>> handlers = new ArrayList<>(4);

    public InteractionUnitClassifier(Peer peer) {
        this.peer = AssertUtil.notNull(peer, "peer");
    }


    public InteractionUnitClassifier<T> handler(Handler<T> handler) {
        handlers.add(handler);
        return this;
    }

    /**
     * Classifies the given execution units using registered handlers.
     * Applies each handler's rule to filter and consume matching units.
     *
     * @param eus the execution units to classify
     */
    public void classify(Map<String, T> eus) {
        if (CollectionUtil.isNotEmpty(eus) && CollectionUtil.isNotEmpty(handlers)) {
            for (Map.Entry<String, T> entry : eus.entrySet()) {
                String key = entry.getKey();
                T value = entry.getValue();
                UnitType<?, ?> type = UnitType.extract(value);
                Class<?> sideType = type.peerType();
                for (Handler<T> handler : handlers) {
                    if (sideType.isAssignableFrom(peer.getClass())
                            && handler.predicate.test(key, value, type, this)) {
                        handler.consumer.accept(key, value);
                    }
                }
            }
        }
    }

    public boolean supportResponse(UnitType<?, ?> type) {
        return type.messageType().isAssignableFrom(peer.protocol().responseType());
    }

    public boolean supportRequest(UnitType<?, ?> type) {
        return type.messageType().isAssignableFrom(peer.protocol().requestType());
    }


    /**
     * Defines a predicate rule to test whether an {@link Executable.Unit}
     * matches certain conditions for the current {@link Peer} and {@link Protocol}.
     *
     * @param <T> the execution unit type
     */
    @FunctionalInterface
    public interface Rule<T extends Interaction.Unit> {
        boolean test(String name, T unit, UnitType<?, ?> unitType, InteractionUnitClassifier<T> classifier);
    }

    /**
     * Defines a handler that applies a {@link Rule} to filter units
     * and consumes matching {@link Executable.Unit}s.
     *
     * @param <T> the execution unit type
     */
    public static final class Handler<T extends Interaction.Unit> {
        public final Rule<T> predicate;
        public final BiConsumer<String, T> consumer;

        private Handler(Rule<T> predicate, BiConsumer<String, T> consumer) {
            this.predicate = predicate;
            this.consumer = consumer;
        }

        public static <T extends Interaction.Unit> Handler<T> of(Rule<T> predicate, BiConsumer<String, T> consumer) {
            return new Handler<>(predicate, consumer);
        }
    }
}
