package io.effi.rpc.transport.codec;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Message;
import io.effi.rpc.transport.WrappedRequest;
import io.effi.rpc.transport.WrappedResponse;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Encodes and decodes messages on the server side.
 */
public interface ServerCodec {

    /**
     * Encodes a wrapped response into a response.
     *
     * @param wrappedResponse the wrapped response
     * @return the encoded response
     */
    Message.Response encode(WrappedResponse<Callee> wrappedResponse);

    /**
     * Decodes a request into a wrapped request.
     *
     * @param channel  the channel
     * @param request the raw request
     * @param callee   the associated callee
     * @return the decoded wrapped request
     */
    WrappedRequest<Callee> decode(Channel channel, Message.Request request, Callee callee);

}


