package io.effi.rpc.transport;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;

/**
 * Represents a wrapped request.
 */
public interface WrappedRequest<T extends CallSide> extends WrappedEnvelope<T, CallContext<Message.Request, T>> {

    /**
     * Encodes the request.
     */
    @Override
    WrappedRequest<T> encode();

    default Message.Request request() {
        return envelope();
    }

    @Override
    Message.Request envelope();
}

