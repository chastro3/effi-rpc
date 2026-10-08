package io.effi.rpc.context;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AbstractAttributes;

/**
 * Defines the interaction model shared by RPC calls and replies.
 */
public interface Interaction {

    /**
     * Creates the reply future used by one interaction.
     *
     * @param context call context
     * @param <T> reply future type
     * @return reply future
     */
    interface Mode<T extends ReplyFuture> {

        /**
         * Creates the reply future for the supplied call context.
         *
         * @param context call context
         * @return reply future
         */
        T newFuture(CallContext<Request, Caller<?>> context);

    }

    /**
     * Carries the message, peer, and module for one interaction.
     *
     * @param <M> message type
     * @param <P> peer type
     */
    abstract class Context<M extends Message, P extends Peer> extends AbstractAttributes implements ScopedModule.Supplier {

        private final ScopedModule module;

        private final M message;

        private final P peer;

        private final Mode<?> mode;

        protected Context(ScopedModule module, M message, P peer, Mode<?> mode) {
            this.module = module;
            this.message = message;
            this.peer = peer;
            this.mode = mode;
        }

        @Override
        public ScopedModule module() {
            return module;
        }

        /**
         * Returns the interaction message.
         */
        public M message() {
            return message;
        }

        /**
         * Returns the interaction peer.
         */
        public P peer() {
            return peer;
        }

        /**
         * Returns the interaction mode.
         */
        public Mode<?> mode() {
            return mode;
        }
    }

    /**
     * Defines one executable unit in an interaction pipeline.
     *
     * @param <M> message type
     * @param <P> peer type
     */
    interface Unit<M extends Message, P extends Peer> {

        /**
         * Returns the unit type, or {@code null} when reflection should infer it.
         */
        default UnitType<M, P> unitType() {
            return null;
        }
    }

    /**
     * Defines execution chains for processing execution units.
     * <p>
     * Provides a chain interface for sequential execution of processing units
     * in RPC message handling pipelines.
     */
    @SuppressWarnings("rawtypes")
    interface UnitChain {

        /**
         * Proceeds from the start of the chain or invokes the next unit.
         *
         * @param context the current exchange context
         * @return the result of processing
         */
        <C extends Interaction.Context> Result proceed(C context);

    }

    /**
     * Represents the result of one interaction.
     */
    interface Result extends io.effi.rpc.concurrent.Result<Object>, SmartURL.Supplier {

        @Override
        SmartURL url();

        @Override
        EffiRpcException cause();

        /**
         * Returns the value or throws the failure cause.
         *
         * @throws EffiRpcException when this result failed
         */
        <T> T excepted();

        /**
         * Creates a successful interaction result.
         *
         * @param url result URL
         * @param result result value
         * @return successful result
         */
        static Result success(SmartURL url, Object result) {
            return DefaultInteractionResult.success(url, result);
        }

        /**
         * Creates a failed interaction result.
         *
         * @param url result URL
         * @param cause failure cause
         * @return failed result
         */
        static Result failure(SmartURL url, EffiRpcException cause) {
            return DefaultInteractionResult.failure(url, cause);
        }
    }
}
