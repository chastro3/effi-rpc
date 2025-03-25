package io.effi.rpc.transport.codec;

import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.transport.RequestWrapper;
import io.effi.rpc.transport.ResponseWrapper;

/**
 * Encode client {@link RequestWrapper} and decode server {@link ResponseWrapper}.
 */
public interface ClientCodec extends Encoder<RequestWrapper<Caller<?>>, Envelope.Request>,
        Decoder<Envelope.Response, ResponseWrapper<Caller<?>>> {

}

