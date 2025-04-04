package io.effi.rpc.transport.codec;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.ReplyFuture;
import io.effi.rpc.transport.RepackagedRequest;
import io.effi.rpc.transport.RepackagedResponse;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Encodes client {@link RepackagedRequest} and decode server {@link RepackagedResponse}.
 */
public interface ClientCodec {

    Envelope.Request encode(RepackagedRequest<Caller<?>> repackagedRequest);

    RepackagedResponse<Caller<?>> decode(Channel channel, Envelope.Response response, ReplyFuture future);

}

