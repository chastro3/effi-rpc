package io.effi.rpc.transport.codec;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.ReplyFuture;
import io.effi.rpc.transport.WrappedRequest;
import io.effi.rpc.transport.WrappedResponse;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Encodes and decodes messages on the client side.
 */
public interface ClientCodec {

    /**
     * Encodes a wrapped request into a request.
     *
     * @param wrappedRequest the wrapped request
     * @return the encoded request
     */
    Envelope.Request encode(WrappedRequest<Caller<?>> wrappedRequest);

    /**
     * Decodes a response into a wrapped response.
     *
     * @param channel  the channel
     * @param response the raw response
     * @param future   the associated future
     * @return the decoded wrapped response
     */
    WrappedResponse<Caller<?>> decode(Channel channel, Envelope.Response response, ReplyFuture future);

}





