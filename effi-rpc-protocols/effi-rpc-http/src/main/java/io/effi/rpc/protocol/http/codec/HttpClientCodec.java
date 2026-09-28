package io.effi.rpc.protocol.http.codec;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.exception.DefaultErrorCode;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpDuplexResponse;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.codec.Decoder;
import io.effi.rpc.transport.codec.Encoder;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettySupport;
import io.effi.rpc.util.Messages;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Implements HTTP client codec for encoding HTTP requests and decoding HTTP responses.
 * - Encodes outbound HTTP requests into network messages.
 * - Decodes inbound HTTP responses into response objects.
 */
public class HttpClientCodec implements Encoder<HttpRequest>, Decoder<HttpResponse, Caller<?>> {


    @Override
    public OutputMessage encode(HttpRequest request, Channel channel) {
        if (request instanceof HttpDuplexRequest httpRequest) {
            HttpHeaders headers = request.headers();
            headers.add(HttpHeaderNames.HOST, request.url().host());
            // todo 是否带上这些?
            //  request.url().addParam(KeyConstant.UNIQUE_ID, String.valueOf(request.url().get(KeyConstant.ATTR_UNIQUE_ID)));
            //  request.url().addParam(KeyConstant.TIMESTAMP, DateUtil.format(LocalDateTime.now()));
            try (ByteBufOutputStream out = NettySupport.newOutputStream((NettyChannel) channel)) {
                HttpUtil.encodeBody(channel.platform(), request, out);
                return httpRequest.channel(channel)
                        .output(out, out.buffer().writerIndex());
            } catch (Exception e) {
                throw TransportErrorCodes.ENCODE.fail(e, OutputMessage.class, request.getClass());
            }
        }
        throw new IllegalStateException(Messages.onlySupport(HttpDuplexRequest.class));
    }

    @Override
    public HttpResponse decode(InputMessage inputMessage, Caller<?> side) {
        try {
            if (inputMessage instanceof HttpDuplexResponse response) {
                ScopedPlatform platform = response.channel().platform();
                Object body = HttpUtil.decodeBody(platform, response, response.inputStream(), side.replyType().type());
                if (response.succeeded()) {
                    response.body(body);
                } else {
                    EffiRpcException fail = error(response, body);
                    response.body(fail);
                }
                return response;
            }
        } catch (Exception e) {
            throw TransportErrorCodes.DECODE.fail(e, HttpResponse.class, inputMessage.getClass());
        }
        throw new IllegalStateException(Messages.onlySupport(HttpDuplexResponse.class));
    }

    private static EffiRpcException error(HttpResponse response, Object body) {
        int statusCode = response.statusCode();
        if (statusCode == HttpResponseStatus.NOT_FOUND.code()) {
            return DefaultErrorCode.valueOf(InteractionErrorCodes.SERVANT_NOT_FOUND.code(), "{}").fail(body);
        }
        if (statusCode == HttpResponseStatus.SERVICE_UNAVAILABLE.code()) {
            return PredefinedErrorCode.SERVICE_UNAVAILABLE.fail(body);
        }
        return InteractionErrorCodes.SERVANT_INVOCATION_FAILED.fail(body);
    }
}
