package io.effi.rpc.core.configurator.classifier;

import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Caller;
import io.effi.rpc.core.PeerDescriptor;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.UnitType;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Classifies interaction units for a peer descriptor.
 * <p>
 * Applies registered handlers to select units that support the peer side and the
 * protocol request or response message type.
 *
 * @param <T> the interaction unit type
 */
@SuppressWarnings("rawtypes")
public class InteractionUnitClassifier<T extends Interaction.Unit> {

    private final PeerDescriptor descriptor;

    private final List<Handler<T>> handlers = new ArrayList<>(4);

    public InteractionUnitClassifier(PeerDescriptor descriptor) {
        this.descriptor = AssertUtil.notNull(descriptor, "descriptor");
    }

    /**
     * Registers a classification handler.
     *
     * @param handler handler to register
     * @return this classifier
     */
    public InteractionUnitClassifier<T> handler(Handler<T> handler) {
        handlers.add(handler);
        return this;
    }

    /**
     * Classifies the supplied interaction units with the registered handlers.
     *
     * @param eus interaction units by extension name
     */
    public void classify(Map<String, T> eus) {
        if (CollectionUtil.isNotEmpty(eus) && CollectionUtil.isNotEmpty(handlers)) {
            for (Map.Entry<String, T> entry : eus.entrySet()) {
                String key = entry.getKey();
                T value = entry.getValue();
                UnitType<?, ?> type = UnitType.extract(value);
                Class<?> sideType = type.peerType();
                Class<?> peerType = descriptor.kind() == PeerDescriptor.Kind.CALLER
                        ? Caller.class
                        : Servant.class;
                for (Handler<T> handler : handlers) {
                    if (sideType.isAssignableFrom(peerType)
                            && handler.predicate.test(key, value, type, this)) {
                        handler.consumer.accept(key, value);
                    }
                }
            }
        }
    }

    /**
     * Returns whether the supplied unit type supports the response type of the peer protocol.
     *
     * @param type interaction unit type
     * @return {@code true} when the response type is supported
     */
    public boolean supportResponse(UnitType<?, ?> type) {
        return type.messageType().isAssignableFrom(descriptor.protocol().responseType());
    }

    /**
     * Returns whether the supplied unit type supports the request type of the peer protocol.
     *
     * @param type interaction unit type
     * @return {@code true} when the request type is supported
     */
    public boolean supportRequest(UnitType<?, ?> type) {
        return type.messageType().isAssignableFrom(descriptor.protocol().requestType());
    }

    /**
     * Defines a predicate that tests whether an interaction unit matches a classification rule.
     *
     * @param <T> the interaction unit type
     */
    @FunctionalInterface
    public interface Rule<T extends Interaction.Unit> {

        /**
         * Tests whether the unit matches this rule.
         *
         * @param name extension name
         * @param unit interaction unit
         * @param unitType extracted unit type
         * @param classifier owning classifier
         * @return {@code true} when the unit matches
         */
        boolean test(String name, T unit, UnitType<?, ?> unitType, InteractionUnitClassifier<T> classifier);
    }

    /**
     * Defines a handler that applies a rule and consumes matching interaction units.
     *
     * @param <T> the interaction unit type
     */
    public static final class Handler<T extends Interaction.Unit> {
        public final Rule<T> predicate;
        public final BiConsumer<String, T> consumer;

        private Handler(Rule<T> predicate, BiConsumer<String, T> consumer) {
            this.predicate = predicate;
            this.consumer = consumer;
        }

        /**
         * Creates a handler from the supplied rule and consumer.
         *
         * @param predicate classification rule
         * @param consumer matching unit consumer
         * @param <T> interaction unit type
         * @return classifier handler
         */
        public static <T extends Interaction.Unit> Handler<T> of(Rule<T> predicate, BiConsumer<String, T> consumer) {
            return new Handler<>(predicate, consumer);
        }
    }
}
