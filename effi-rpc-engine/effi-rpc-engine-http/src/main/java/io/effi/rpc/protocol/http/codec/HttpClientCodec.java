package io.effi.rpc.protocol.http.codec;

import io.effi.rpc.config.URL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.util.DateUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Result;
import io.effi.rpc.contract.parameter.ReplyParser;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.RepackagedRequest;
import io.effi.rpc.transport.codec.AbstractClientCodec;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.time.LocalDateTime;

/**
 * HTTP client codec for encoding HTTP requests and decoding HTTP responses.
 * Implements {@link ReplyParser} to resolve the response and provide the
 * final result.
 */
public class HttpClientCodec extends AbstractClientCodec<HttpRequest<Object>, HttpResponse<ByteBuf>>
        implements ReplyParser<HttpResponse<ByteBuf>> {

    public HttpClientCodec() {
        this.replyParser = this;
    }

    @Override
    protected Envelope.Request encodeRequest(RepackagedRequest<Caller<?>> repackagedRequest, HttpRequest<Object> request) throws Exception {
        HttpHeaders headers = request.headers();
        headers.add(HttpHeaderNames.HOST, request.url().host());
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
                ? new Result(url, replyValue)
                : new Result(url, PredefinedErrorCode.INVOKE_SERVICE.fail(null, String.valueOf(replyValue)));
    }
}
