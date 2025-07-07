package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Message;
import io.effi.rpc.base.context.CallContext;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLType;
import io.effi.rpc.protocol.http.HttpCaller;
import io.effi.rpc.protocol.http.support.HttpRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.effi.rpc.protocol.http.support.HttpVersion;
import io.effi.rpc.transport.netty.NettyChannel;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    private static final String REQUEST_STREAM_PREFIX = "request-stream-";

    private static final String RESPONSE_STREAM_PREFIX = "response-stream-";

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
        String streamKey = REQUEST_STREAM_PREFIX + stream.id();
        NettyChannel nettyChannel = NettyChannel.get(ctx.channel());
        return getOrCreateStream(streamKey, ctx, () -> new Http2RequestStream(nettyChannel, stream));
    }

    /**
     * Gets or creates http2 response stream from channel,Create if it doesn't exist.
     */
    public static Http2ResponseStream getOrCreateResponseStream(ChannelHandlerContext ctx, Http2FrameStream stream) {
        String streamKey = RESPONSE_STREAM_PREFIX + stream.id();
        return getOrCreateStream(streamKey, ctx, () -> new Http2ResponseStream(stream));
    }

    /**
     * Removes http2 request stream from channel.
     */
    public static void removeRequestStream(ChannelHandlerContext ctx, Http2RequestStream requestStream) {
        removeStream(ctx, REQUEST_STREAM_PREFIX + requestStream.stream().id());
    }

    /**
     * Removes http2 response stream from channel.
     */
    public static void removeResponseStream(ChannelHandlerContext ctx, Http2ResponseStream responseStream) {
        removeStream(ctx, RESPONSE_STREAM_PREFIX + responseStream.stream().id());
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
    public static Http2StreamFrame[] toHttp2StreamFrames(HttpRequest<byte[]> request) {
        URL url = request.url();
        // build http2 headers
        Http2Headers http2Headers = ((NettyHttp2Headers) request.headers()).headers();
        http2Headers.scheme(url.standardProtocol());
        http2Headers.method(request.method().name());
        http2Headers.path(url.queryPath());
        // wrapper http2 body
        ByteBuf data = Unpooled.wrappedBuffer(request.body());
        return toHttp2StreamFrames(http2Headers, data);
    }

    /**
     * Converts to netty's http2 stream frames.
     */
    public static Http2StreamFrame[] toHttp2StreamFrames(HttpResponse<byte[]> response) {
        // build http2 headers
        Http2Headers http2Headers = ((NettyHttp2Headers) response.headers()).headers();
        http2Headers.status(HttpResponseStatus.valueOf(response.statusCode()).codeAsText());
        // wrapper http2 body
        ByteBuf data = Unpooled.wrappedBuffer(response.body());
        return toHttp2StreamFrames(http2Headers, data);
    }

    /**
     * Converts from netty's http2 stream.
     */
    public static HttpResponse<ByteBuf> fromHttp2ResponseStream(Http2ResponseStream responseStream, CallContext<Message.Request, Caller<?>> context) {
        HttpCaller<?> httpCaller = (HttpCaller<?>) context.callSide();
        return HttpResponse.<ByteBuf>builder()
                .version(HttpVersion.HTTP_2_0)
                .method(httpCaller.httpMethod())
                .statusCode(responseStream.statusCode())
                .url(context.message().url())
                .headers(responseStream.headers())
                .body(responseStream.body())
                .build();
    }

    /**
     * Converts from netty's http2 stream.
     */
    public static HttpRequest<ByteBuf> fromHtt2RequestStream(Http2RequestStream requestStream) {
        return HttpRequest.<ByteBuf>builder()
                .version(HttpVersion.HTTP_2_0)
                .method(requestStream.method())
                .url(requestStream.url())
                .headers(requestStream.headers)
                .body(requestStream.body())
                .build();
    }

    /**
     * Builds http2 settings.
     */
    public static Http2Settings createHttp2Settings(URL url) {
        int initialWindows = url.getIntParam(DefaultConfigNames.INITIAL_WINDOW_SIZE);
        long maxConcurrentStreams = url.getLongParam(DefaultConfigNames.MAX_CONCURRENT_STREAMS);
        int maxFrameSize = url.getIntParam(DefaultConfigNames.MAX_FRAME_SIZE);
        int maxHeaderListSize = url.getIntParam(DefaultConfigNames.MAX_HEADER_LIST_SIZE);
        long headerTableSize = url.getLongParam(DefaultConfigNames.HEADER_TABLE_SIZE);
        Http2Settings settings = new Http2Settings();
        settings.initialWindowSize(initialWindows);
        settings.maxConcurrentStreams(maxConcurrentStreams);
        settings.maxFrameSize(maxFrameSize);
        settings.maxHeaderListSize(maxHeaderListSize);
        settings.headerTableSize(headerTableSize);
        if (URLType.CLIENT.match(url)) {
            boolean pushEnabled = url.getBooleanParam(DefaultConfigNames.PUSH_ENABLED);
            settings.pushEnabled(pushEnabled);
        }
        return settings;
    }

    private static <T extends Http2MessageStream> T getOrCreateStream(String streamKey, ChannelHandlerContext ctx, Supplier<T> creator) {
        AttributeKey<T> streamAttributeKey = AttributeKey.valueOf(streamKey);
        Attribute<T> streamAttribute = ctx.channel().attr(streamAttributeKey);
        T nettyHttp2Stream = streamAttribute.get();
        if (nettyHttp2Stream == null) {
            nettyHttp2Stream = creator.get();
            streamAttribute.set(nettyHttp2Stream);
        }
        return nettyHttp2Stream;
    }

    private static void removeStream(ChannelHandlerContext ctx, String streamKey) {
        AttributeKey<Http2MessageStream> streamAttributeKey = AttributeKey.valueOf(streamKey);
        Attribute<Http2MessageStream> streamMessageAttribute = ctx.channel().attr(streamAttributeKey);
        streamMessageAttribute.set(null);
    }

}
