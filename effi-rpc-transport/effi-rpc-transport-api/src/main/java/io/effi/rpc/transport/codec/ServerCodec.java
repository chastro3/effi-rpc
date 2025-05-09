package io.effi.rpc.transport.codec;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.transport.WrappedRequest;
import io.effi.rpc.transport.WrappedResponse;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Encodes and decodes RPC requests and responses on the server side.
 */
public interface ServerCodec {

    /**
     * Encodes the given wrapped response into a response.
     *
     * @param wrappedResponse the response to encode
     * @return the encoded response envelope
     */
    Envelope.Response encode(WrappedResponse<Callee<?>> wrappedResponse);

    /**
     * Decodes the incoming request into a wrapped request.
     *
     * @param channel the communication channel
     * @param request the original request
     * @param callee  the callee handling the request
     * @return the repackaged request
     */
    WrappedRequest<Callee<?>> decode(Channel channel, Envelope.Request request, Callee<?> callee);

}


