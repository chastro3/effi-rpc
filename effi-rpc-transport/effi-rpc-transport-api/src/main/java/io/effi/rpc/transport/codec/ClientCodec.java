package io.effi.rpc.transport.codec;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.ReplyFuture;
import io.effi.rpc.transport.WrappedRequest;
import io.effi.rpc.transport.WrappedResponse;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Encodes and decodes RPC requests and responses on the client side.
 */
public interface ClientCodec {

    /**
     * Encodes the given wrapped request into a request.
     *
     * @param wrappedRequest the request to encode
     * @return the encoded request envelope
     */
    Envelope.Request encode(WrappedRequest<Caller<?>> wrappedRequest);

    /**
     * Decodes the incoming response into a wrapped response.
     *
     * @param channel  the communication channel
     * @param response the original response
     * @param future   the associated reply future
     * @return the wrapped response
     */
    WrappedResponse<Caller<?>> decode(Channel channel, Envelope.Response response, ReplyFuture future);

}





