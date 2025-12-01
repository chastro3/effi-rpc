package io.effi.rpc.context.support;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.Request;
import io.effi.rpc.exception.EffiRpcException;

import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

public interface Unary {

    Interaction.Mode<ReplyFuture> MODE = ReplyFuture::new;

    class ReplyFuture extends io.effi.rpc.context.ReplyFuture {

        private final AtomicInteger errorCount = new AtomicInteger(0);

        private FailureHandler failureHandler;

        public ReplyFuture(CallContext<Request, Caller<?>> context) {
            super(context);
        }

        public ReplyFuture failureHandler(FailureHandler failureHandler) {
            this.failureHandler = failureHandler;
            return this;
        }

        public int errorCount() {
            return errorCount.get();
        }

        @Override
        public ReplyFuture failure(EffiRpcException cause) {
            if (failureHandler != null && !completed()) {
                try {
                    errorCount.incrementAndGet();
                    failureHandler.handle(this, cause);
                } catch (EffiRpcException e) {
                    super.failure(cause);
                }
            }
            return this;
        }
    }

    /**
     * Handles failures by processing {@link EffiRpcException} during RPC execution.
     */
    @Extensible(scope = PLATFORM)
    interface FailureHandler {

        /**
         * Handles the failure when an {@link EffiRpcException} is thrown.
         *
         * @param e the exception encountered during RPC execution
         */
        void handle(ReplyFuture future, EffiRpcException e) throws EffiRpcException;
    }
}
