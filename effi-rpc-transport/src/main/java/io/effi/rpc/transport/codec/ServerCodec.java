package io.effi.rpc.transport.codec;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.transport.RequestWrapper;
import io.effi.rpc.transport.ResponseWrapper;

/**
 * Encode server {@link ResponseWrapper} and decode client {@link RequestWrapper}.
 */
public interface ServerCodec extends Encoder<ResponseWrapper<Callee<?>>, Envelope.Response>,
        Decoder<Envelope.Request, RequestWrapper<Callee<?>>> {

}
