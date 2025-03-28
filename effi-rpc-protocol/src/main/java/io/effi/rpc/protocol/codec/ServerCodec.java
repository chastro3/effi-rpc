package io.effi.rpc.protocol.codec;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.protocol.RequestWrapper;
import io.effi.rpc.protocol.ResponseWrapper;

/**
 * Encode server {@link ResponseWrapper} and decode client {@link RequestWrapper}.
 */
public interface ServerCodec extends Encoder<ResponseWrapper<Callee<?>>, Envelope.Response>,
        Decoder<Envelope.Request, RequestWrapper<Callee<?>>> {

}
