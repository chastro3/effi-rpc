package io.effi.rpc.context.support;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.support.failure.FailFast;
import io.effi.rpc.exception.EffiRpcException;

import java.util.concurrent.atomic.AtomicInteger;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

public class UnaryReplyFuture extends ReplyFuture {

    private final AtomicInteger errorCount = new AtomicInteger(0);

    private FailureHandler failureHandler;

    public UnaryReplyFuture(CallContext<Request, Caller<?>> context) {
        super(context);
    }

    public UnaryReplyFuture withFailureHandler(FailureHandler failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

    public int errorCount() {
        return errorCount.get();
    }

    @Override
    public UnaryReplyFuture failure(EffiRpcException cause) {
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

    /**
     * Handles failures by processing {@link EffiRpcException} during RPC execution.
     */
    @Extensible(value = FailFast.NAME, scope = PLATFORM)
    public interface FailureHandler {

        /**
         * Handles the failure when an {@link EffiRpcException} is thrown.
         *
         * @param e the exception encountered during RPC execution
         */
        void handle(UnaryReplyFuture future, EffiRpcException e) throws EffiRpcException;
    }

}
