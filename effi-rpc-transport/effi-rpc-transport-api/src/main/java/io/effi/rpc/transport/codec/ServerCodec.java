package io.effi.rpc.transport.codec;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.transport.RepackagedRequest;
import io.effi.rpc.transport.RepackagedResponse;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Server-side codec for encoding and decoding requests and responses.
 */
public interface ServerCodec {

    /**
     * Encodes to response from repackaged response.
     *
     * @param repackagedResponse the repackaged response
     * @return the encoded response
     */
    Envelope.Response encode(RepackagedResponse<Callee<?>> repackagedResponse);

    /**
     * Decodes to repackaged request from request.
     *
     * @param channel the channel
     * @param request the request
     * @param callee  the callee
     * @return the decoded repackaged request
     */
    RepackagedRequest<Callee<?>> decode(Channel channel, Envelope.Request request, Callee<?> callee);
}


