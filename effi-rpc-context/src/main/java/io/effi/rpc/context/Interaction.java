package io.effi.rpc.context;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.support.DefaultInteractionResult;
import io.effi.rpc.context.support.ReplyFuture;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AbstractAttributes;

public interface Interaction {

    interface Mode<T extends ReplyFuture> {

        T newFuture(CallContext<Request, Caller<?>> context);

    }

    abstract class Context<M extends Message, P extends Peer> extends AbstractAttributes implements ScopedModule.Supplier {

        private final ScopedModule module;

        private final P peer;

        private final M message;

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

        public M message() {
            return message;
        }

        public P peer() {
            return peer;
        }

        public Mode<?> mode() {
            return mode;
        }
    }


    interface Unit<M extends Message, P extends Peer> {

        /**
         * Specifies this unit's type.
         * <p>Defaults to {@code null}. Override to avoid reflection.</p>
         *
         * @return the unit type or {@code null} if unspecified
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

    interface Result extends io.effi.rpc.async.Result<Object>, SmartURL.Supplier {

        @Override
        SmartURL url();

        @Override
        EffiRpcException cause();

        <T> T excepted();

        static Result success(SmartURL url, Object result) {
            return DefaultInteractionResult.success(url, result);
        }

        static Result failure(SmartURL url, EffiRpcException cause) {
            return DefaultInteractionResult.failure(url, cause);
        }
    }
}
