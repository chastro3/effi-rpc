package io.effi.rpc.transport;

import io.effi.rpc.base.CallSide;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.ReplyContext;

/**
 * Represents a wrapped response.
 */
public interface WrappedResponse<T extends CallSide> extends WrappedEnvelope<T, ReplyContext<Message.Response, T>> {

    /**
     * Encodes the response.
     */
    @Override
    WrappedResponse<T> encode();

    default Message.Response response() {
        return envelope();
    }

    @Override
    Message.Response envelope();
}
