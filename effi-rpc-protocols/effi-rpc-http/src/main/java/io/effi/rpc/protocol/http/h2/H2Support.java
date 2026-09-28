package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.protocol.http.HttpCaller;
import io.effi.rpc.protocol.http.HttpOptions;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpDuplexResponse;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettySupport;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2FrameStream;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;
import io.netty.handler.codec.http2.Http2StreamFrame;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.function.Supplier;

/**
 * Utility class for http2 operations.
 */
public class H2Support {

    public static final AttributeKey<Http2StreamChannelBootstrap> H2_STREAM_BOOTSTRAP_KEY = AttributeKey.valueOf("h2-stream-bootstrap");

    public static final String[] SUPPORTED_PROTOCOL = new String[]{ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1};

    private static final AttributeKey<Http2RequestStream> REQUEST_STREAM_KEY =
            AttributeKey.valueOf("effi-rpc.h2.request-stream");

    private static final AttributeKey<Http2ResponseStream> RESPONSE_STREAM_KEY =
            AttributeKey.valueOf("effi-rpc.h2.response-stream");

    public static void bindStreamBootstrap(Channel channel, Http2StreamChannelBootstrap streamBootstrap) {
        channel.attr(H2_STREAM_BOOTSTRAP_KEY).set(streamBootstrap);
    }

    public static Http2StreamChannelBootstrap getBoundStreamBootstrap(Channel channel) {
        Attribute<Http2StreamChannelBootstrap> attr = channel.attr(H2_STREAM_BOOTSTRAP_KEY);
        return attr.get();
    }

    /**
     * Gets or creates http2 request stream from channel,Create if it doesn't exist.
     * todo 优化为null的时候
     */
    public static Http2RequestStream getOrCreateRequestStream(ChannelHandlerContext ctx, Http2FrameStream stream) {
        NettyChannel nettyChannel = NettyChannel.ensure(ctx.channel());
        return getOrCreateStream(
                ctx,
                REQUEST_STREAM_KEY,
                () -> new Http2RequestStream(
                        nettyChannel,
                        stream,
                        nettyChannel.endpoint().config().option(HttpOptions.MAX_MESSAGE_SIZE)
                )
        );
    }

    /**
     * Gets or creates http2 response stream from channel,Create if it doesn't exist.
     */
    public static Http2ResponseStream getOrCreateResponseStream(ChannelHandlerContext ctx, Http2FrameStream stream) {
        NettyChannel nettyChannel = NettyChannel.ensure(ctx.channel());
        return getOrCreateStream(
                ctx,
                RESPONSE_STREAM_KEY,
                () -> new Http2ResponseStream(
                        stream,
                        nettyChannel.endpoint().config().option(HttpOptions.MAX_MESSAGE_SIZE)
                )
        );
    }

    /**
     * Removes http2 request stream from channel.
     */
    public static void removeRequestStream(ChannelHandlerContext ctx) {
        removeStream(ctx, REQUEST_STREAM_KEY);
    }

    /**
     * Removes http2 response stream from channel.
     */
    public static void removeResponseStream(ChannelHandlerContext ctx) {
        removeStream(ctx, RESPONSE_STREAM_KEY);
    }

    public static void releaseRequestStream(ChannelHandlerContext ctx) {
        closeStream(ctx, REQUEST_STREAM_KEY);
    }

    public static void releaseResponseStream(ChannelHandlerContext ctx) {
        closeStream(ctx, RESPONSE_STREAM_KEY);
    }

    /**
     * Converts http2 headers and data to http2 stream frames.
     */
    public static Http2StreamFrame[] toHttp2StreamFrames(Http2Headers headers, ByteBuf data) {
        boolean headersEndStream = !data.isReadable();
        Http2StreamFrame headersFrame = new DefaultHttp2HeadersFrame(headers, headersEndStream);
        Http2StreamFrame dataFrame = headersEndStream ? null : new DefaultHttp2DataFrame(data, true);
        return headersEndStream ? new Http2StreamFrame[]{headersFrame} : new Http2StreamFrame[]{headersFrame, dataFrame};
    }

    /**
     * Converts to netty's http2 stream frames.
     */
    public static Http2StreamFrame[] toHttp2StreamFrames(HttpDuplexRequest request) {
        SmartURL smartUrl = request.url();
        // build http2 headers
        Http2Headers http2Headers = ((NettyHttp2Headers) request.headers()).headers();
        http2Headers.scheme(request.version().schema());
        http2Headers.method(request.method().name());
        http2Headers.path(smartUrl.queryPath());
        // wrapper http2 body
        return toHttp2StreamFrames(http2Headers, NettySupport.toByteBuf(request.outputStream()));
    }

    /**
     * Converts to netty's http2 stream frames.
     */
    public static Http2StreamFrame[] toHttp2StreamFrames(HttpDuplexResponse response) {
        // build http2 headers
        Http2Headers http2Headers = ((NettyHttp2Headers) response.headers()).headers();
        http2Headers.status(HttpResponseStatus.valueOf(response.statusCode()).codeAsText());
        // wrapper http2 body
        return toHttp2StreamFrames(http2Headers, NettySupport.toByteBuf(response.outputStream()));
    }

    /**
     * Converts from netty's http2 stream.
     */
    public static HttpDuplexResponse fromHttp2ResponseStream(Http2ResponseStream responseStream, ChannelHandlerContext ctx, CallContext<Request, Caller<?>> context) {
        HttpCaller<?> httpCaller = (HttpCaller<?>) context.peer();
        return HttpDuplexResponse.builder()
                .version(Http2Protocol.VERSION)
                .method(httpCaller.httpMethod())
                .statusCode(responseStream.statusCode())
                .url(context.message().url())
                .headers(responseStream.headers())
                .build()
                .channel(NettyChannel.ensure(ctx.channel()))
                .input(NettySupport.newInputStream(responseStream.takeBody()));
    }

    /**
     * Converts from netty's http2 stream.
     */
    public static HttpDuplexRequest fromHtt2RequestStream(Http2RequestStream requestStream, ChannelHandlerContext ctx) {
        return HttpDuplexRequest.builder()
                .version(Http2Protocol.VERSION)
                .method(requestStream.method())
                .url(requestStream.url())
                .headers(requestStream.headers)
                .build()
                .channel(NettyChannel.ensure(ctx.channel()))
                .input(NettySupport.newInputStream(requestStream.takeBody()));
    }

    /**
     * Builds http2 settings.
     */
    public static Http2Settings createHttp2Settings(EndpointConfig config, boolean isClient) {
        int initialWindows = config.option(Http2Options.INITIAL_WINDOW_SIZE);
        long maxConcurrentStreams = config.option(Http2Options.MAX_CONCURRENT_STREAMS);
        int maxFrameSize = config.option(Http2Options.MAX_FRAME_SIZE);
        int maxHeaderListSize = config.option(Http2Options.MAX_HEADER_LIST_SIZE);
        long headerTableSize = config.option(Http2Options.HEADER_TABLE_SIZE);
        Http2Settings settings = new Http2Settings();
        settings.initialWindowSize(initialWindows);
        settings.maxConcurrentStreams(maxConcurrentStreams);
        settings.maxFrameSize(maxFrameSize);
        settings.maxHeaderListSize(maxHeaderListSize);
        settings.headerTableSize(headerTableSize);
        if (isClient) {
            boolean pushEnabled = config.option(Http2Options.PUSH_ENABLED);
            settings.pushEnabled(pushEnabled);
        }
        return settings;
    }

    private static <T extends Http2MessageStream> T getOrCreateStream(ChannelHandlerContext ctx, AttributeKey<T> streamAttributeKey, Supplier<T> creator) {
        Attribute<T> streamAttribute = ctx.channel().attr(streamAttributeKey);
        T nettyHttp2Stream = streamAttribute.get();
        if (nettyHttp2Stream == null) {
            nettyHttp2Stream = creator.get();
            streamAttribute.set(nettyHttp2Stream);
        }
        return nettyHttp2Stream;
    }

    private static <T extends Http2MessageStream> T removeStream(ChannelHandlerContext ctx, AttributeKey<T> streamAttributeKey) {
        return ctx.channel().attr(streamAttributeKey).getAndSet(null);
    }

    private static <T extends Http2MessageStream> void closeStream(ChannelHandlerContext ctx, AttributeKey<T> streamAttributeKey) {
        T stream = removeStream(ctx, streamAttributeKey);
        if (stream != null) {
            stream.close();
        }
    }

}
