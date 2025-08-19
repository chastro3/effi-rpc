package io.effi.rpc.protocol.http.codec;

import io.effi.rpc.protocol.http.HttpCallee;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpDuplexResponse;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.transport.codec.Decoder;
import io.effi.rpc.transport.codec.Encoder;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.transport.message.InputMessage;
import io.effi.rpc.transport.message.OutputMessage;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettySupport;
import io.effi.rpc.util.Messages;
import io.netty.buffer.ByteBufOutputStream;

import java.io.IOException;

/**
 * Implements HTTP server codec for encoding HTTP responses and decoding HTTP requests.
 * - Encodes outbound HTTP responses into network messages.
 * - Decodes inbound HTTP requests into request objects.
 */
public class HttpServerCodec implements Encoder<HttpResponse>, Decoder<HttpRequest, HttpCallee> {

    @Override
    public OutputMessage encode(HttpResponse response, Channel channel) {
        if (response instanceof HttpDuplexResponse httpResponse) {
            try (ByteBufOutputStream out = NettySupport.newOutputStream((NettyChannel) channel)) {
                HttpUtil.encodeBody(channel.platform(), response, out);
                return httpResponse.withChannel(channel)
                        .withOutput(out, out.buffer().writerIndex());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalStateException(Messages.onlySupport(HttpDuplexResponse.class));
    }

    @Override
    public HttpRequest decode(InputMessage inputMessage, HttpCallee callee) {
        if (inputMessage instanceof HttpDuplexRequest request) {
            return request;
        }
        throw new IllegalStateException(Messages.onlySupport(HttpDuplexRequest.class));
    }
}
