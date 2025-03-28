package io.effi.rpc.protocol.codec;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.protocol.RequestWrapper;
import io.effi.rpc.protocol.ResponseWrapper;

/**
 * Encode client {@link RequestWrapper} and decode server {@link ResponseWrapper}.
 */
public interface ClientCodec extends Encoder<RequestWrapper<Caller<?>>, Envelope.Request>,
        Decoder<Envelope.Response, ResponseWrapper<Caller<?>>> {

}

