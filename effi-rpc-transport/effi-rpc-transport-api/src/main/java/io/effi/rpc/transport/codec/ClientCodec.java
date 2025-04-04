package io.effi.rpc.transport.codec;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.ReplyFuture;
import io.effi.rpc.transport.RepackagedRequest;
import io.effi.rpc.transport.RepackagedResponse;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Client-side codec for encoding and decoding requests and responses.
 */
public interface ClientCodec {

    /**
     * Encodes to request from repackaged request.
     *
     * @param repackagedRequest the repackaged request
     * @return the encoded request
     */
    Envelope.Request encode(RepackagedRequest<Caller<?>> repackagedRequest);

    /**
     * Decodes to repackaged response from response.
     *
     * @param channel  the channel
     * @param response the response
     * @param future   the future
     * @return the decoded repackaged response
     */
    RepackagedResponse<Caller<?>> decode(Channel channel, Envelope.Response response, ReplyFuture future);
}





