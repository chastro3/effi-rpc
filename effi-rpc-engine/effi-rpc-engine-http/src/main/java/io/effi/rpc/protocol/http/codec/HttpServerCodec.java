package io.effi.rpc.protocol.http.codec;

import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.RepackagedResponse;
import io.effi.rpc.transport.codec.AbstractServerCodec;
import io.netty.buffer.ByteBuf;

/**
 * HTTP server codec for encoding HTTP responses and decoding HTTP requests.
 */
public class HttpServerCodec extends AbstractServerCodec<HttpResponse<Object>, HttpRequest<ByteBuf>> {

    @Override
    protected Envelope.Response encodeResponse(RepackagedResponse<Callee<?>> repackagedResponse, HttpResponse<Object> response) throws Exception {
        return response.body(HttpUtil.encodeBody(response));
    }

}
