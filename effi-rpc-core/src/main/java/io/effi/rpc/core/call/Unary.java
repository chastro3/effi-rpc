package io.effi.rpc.core.call;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Interaction;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.context.Request;
import io.effi.rpc.exception.EffiRpcException;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Defines unary call primitives and failure handling.
 */
public interface Unary {

    Interaction.Mode<ReplyFuture> MODE = ReplyFuture::new;

    /**
     * Handles failures by processing {@link EffiRpcException} during RPC execution.
     */
    @Extensible(scope = PLATFORM)
    interface FailureHandler {

        /**
         * Handles the failure when an {@link EffiRpcException} is thrown.
         *
         * @param context the failed call context
         * @param failureCount the number of failed attempts
         * @param cause the exception encountered during RPC execution
         * @throws EffiRpcException when the failure should terminate the call
         */
        void handle(CallContext<Request, Caller<?>> context, int failureCount, EffiRpcException cause) throws EffiRpcException;
    }
}
