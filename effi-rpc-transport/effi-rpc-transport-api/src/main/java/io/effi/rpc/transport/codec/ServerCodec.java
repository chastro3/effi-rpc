package io.effi.rpc.transport.codec;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.transport.RepackagedRequest;
import io.effi.rpc.transport.RepackagedResponse;
import io.effi.rpc.transport.endpoint.Channel;

/**
 * Encodes server {@link RepackagedResponse} and decode client {@link RepackagedRequest}.
 */
public interface ServerCodec {

    Envelope.Response encode(RepackagedResponse<Callee<?>> repackagedResponse);

    RepackagedRequest<Callee<?>> decode(Channel channel, Envelope.Request request, Callee<?> callee);

}
