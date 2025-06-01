package io.effi.rpc.protocol.http.codec;

import io.effi.rpc.config.URL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.Result;
import io.effi.rpc.base.ResultType;
import io.effi.rpc.base.parameter.ReplyParser;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.WrappedRequest;
import io.effi.rpc.transport.codec.AbstractClientCodec;
import io.effi.rpc.util.DateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.time.LocalDateTime;

/**
 * Implements HTTP client codec for encoding HTTP requests and decoding HTTP responses.
 * - Encodes outbound HTTP requests into network messages.
 * - Decodes inbound HTTP responses into response objects.
 * Implements {@link ReplyParser} to resolve the response and provide the final result.
 */
public class HttpClientCodec extends AbstractClientCodec<HttpRequest<Object>, HttpResponse<ByteBuf>>
        implements ReplyParser<HttpResponse<ByteBuf>> {

    public HttpClientCodec() {
        this.replyParser = this;
    }

    @Override
    protected Envelope.Request encodeRequest(WrappedRequest<Caller<?>> wrappedRequest, HttpRequest<Object> request) throws Exception {
        HttpHeaders headers = request.headers();
        headers.add(HttpHeaderNames.HOST, request.url().host());
        // todo 是否带上这些?
        request.url().addParam(KeyConstant.UNIQUE_ID, String.valueOf(request.url().get(KeyConstant.ATTR_UNIQUE_ID)));
        request.url().addParam(KeyConstant.TIMESTAMP, DateUtil.format(LocalDateTime.now()));
        return request.body(HttpUtil.encodeBody(request));
    }

    @Override
    public Result resolve(HttpResponse<ByteBuf> response, Caller<?> caller) {
        Object replyValue = HttpUtil.decodeBody(response, caller.returnType().type());
        response.body(replyValue);
        URL url = response.url();
        return response.isSuccess()
                ? ResultType.VALUE.createResult(url, replyValue)
                : ResultType.EXCEPTION.createResult(url, PredefinedErrorCode.INVOKE_SERVICE.fail(null, String.valueOf(replyValue)));
    }
}
