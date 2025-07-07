package io.effi.rpc.protocol.http.codec;

import io.effi.rpc.base.Callee;
import io.effi.rpc.base.Message;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.WrappedResponse;
import io.effi.rpc.transport.codec.AbstractServerCodec;
import io.netty.buffer.ByteBuf;

/**
 * Implements HTTP server codec for encoding HTTP responses and decoding HTTP requests.
 * - Encodes outbound HTTP responses into network messages.
 * - Decodes inbound HTTP requests into request objects.
 */
public class HttpServerCodec extends AbstractServerCodec<HttpResponse<Object>, HttpRequest<ByteBuf>> {

    @Override
    protected Message.Response encodeResponse(WrappedResponse<Callee> wrappedResponse, HttpResponse<Object> response) throws Exception {
        return response.body(HttpUtil.encodeBody(response));
    }

}
