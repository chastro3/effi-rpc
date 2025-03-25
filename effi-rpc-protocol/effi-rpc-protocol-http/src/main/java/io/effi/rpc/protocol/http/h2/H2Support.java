package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.url.URLType;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.protocol.http.HttpCaller;
import io.effi.rpc.protocol.http.support.*;
import io.effi.rpc.transport.InitializedConfig;
import io.effi.rpc.transport.NettyChannel;
import io.effi.rpc.transport.NettySupport;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Utility class for http2 operations.
 */
public class H2Support {

    public static final AttributeKey<Http2StreamChannelBootstrap> H2_STREAM_BOOTSTRAP_KEY = AttributeKey.valueOf("h2-stream-bootstrap");

    private static final String REQUEST_STREAM_PREFIX = "request-stream-";

    private static final String RESPONSE_STREAM_PREFIX = "response-stream-";

    /**
     * Creates http2 stream channel.
     *
     * @param bootstrap
     * @param endpointUrl
     * @param module
     * @param connectTimeout
     * @return
     */
    public static NettyChannel acquireStreamChannel(Http2StreamChannelBootstrap bootstrap, URL endpointUrl, EffiRpcModule module, int connectTimeout) {
        try {
            Http2StreamChannel streamChannel = bootstrap.open().get(connectTimeout, TimeUnit.MILLISECONDS);
            return NettyChannel.acquire(streamChannel, endpointUrl, module);
        } catch (Exception e) {
            throw EffiRpcException.wrap(
                    PredefinedErrorCode.ACQUIRE_CHANNEL,
                    e,
                    endpointUrl.address(),
                    endpointUrl.protocol()
            );
        }
    }

    /**
     * Builds http2 client channel initializer.
     *
     * @param config
     * @param streamHandler
     * @return
     */
    public static ChannelInitializer<SocketChannel> buildClietnChannelInitializer(InitializedConfig config, ChannelHandler streamHandler) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                initClientChannel(ch, config, streamHandler);
            }
        };
    }

    /**
     * Builds http2 channel pool handler.
     *
     * @param config
     * @param streamHandler
     * @return
     */
    public static ChannelPoolHandler buildChannelPoolHandler(InitializedConfig config, ChannelHandler streamHandler) {
        return new AbstractChannelPoolHandler() {
            @Override
            public void channelCreated(Channel ch) throws Exception {
                initClientChannel(ch, config, streamHandler);
            }
        };
    }

    /**
     * Initializes http2 client channel.
     *
     * @param channel
     * @param config
     * @param streamHandler
     */
    public static void initClientChannel(Channel channel, InitializedConfig config, ChannelHandler streamHandler) {
        NettySupport.initClientChannel(channel, config);
        var streamChannelBootstrap = new Http2StreamChannelBootstrap(channel);
        streamChannelBootstrap.handler(streamHandler);
        channel.attr(H2_STREAM_BOOTSTRAP_KEY).set(streamChannelBootstrap);
    }

    /**
     * Acquires http2 request stream from channel,Create if it doesn't exist.
     *
     * @param ctx
     * @param stream
     * @return
     */
    public static Http2RequestStream acquireRequestStream(ChannelHandlerContext ctx, Http2FrameStream stream) {
        String streamKey = REQUEST_STREAM_PREFIX + stream.id();
        NettyChannel nettyChannel = NettyChannel.acquire(ctx.channel());
        return acquireStream(streamKey, ctx, () -> new Http2RequestStream(nettyChannel, stream));
    }

    /**
     * Acquires http2 response stream from channel,Create if it doesn't exist.
     *
     * @param ctx
     * @param stream
     * @return
     */
    public static Http2ResponseStream acquireResponseStream(ChannelHandlerContext ctx, Http2FrameStream stream) {
        String streamKey = RESPONSE_STREAM_PREFIX + stream.id();
        return acquireStream(streamKey, ctx, () -> new Http2ResponseStream(stream));
    }

    /**
     * Removes http2 request stream from channel.
     *
     * @param ctx
     * @param requestStream
     */
    public static void removeRequestStream(ChannelHandlerContext ctx, Http2RequestStream requestStream) {
        removeStream(ctx, REQUEST_STREAM_PREFIX + requestStream.stream().id());
    }

    /**
     * Removes http2 response stream from channel.
     *
     * @param ctx
     * @param responseStream
     */
    public static void removeResponseStream(ChannelHandlerContext ctx, Http2ResponseStream responseStream) {
        removeStream(ctx, RESPONSE_STREAM_PREFIX + responseStream.stream().id());
    }

    /**
     * Converts http2 headers and data to http2 stream frames.
     *
     * @param headers
     * @param data
     * @return
     */
    public static Http2StreamFrame[] toHttp2StreamFrames(Http2Headers headers, ByteBuf data) {
        boolean headersEndStream = !data.isReadable();
        Http2StreamFrame headersFrame = new DefaultHttp2HeadersFrame(headers, headersEndStream);
        Http2StreamFrame dataFrame = headersEndStream ? null : new DefaultHttp2DataFrame(data, true);
        return headersEndStream ? new Http2StreamFrame[]{headersFrame} : new Http2StreamFrame[]{headersFrame, dataFrame};
    }

    /**
     * Converts to netty's http2 stream frames.
     *
     * @param request
     * @return
     */
    public static Http2StreamFrame[] toHttp2StreamFrames(HttpRequest<byte[]> request) {
        URL url = request.url();
        // build http2 headers
        Http2Headers http2Headers = toHttp2Headers(request.headers());
        http2Headers.scheme(HttpUtil.toStandardScheme(url));
        http2Headers.method(request.method().name());
        http2Headers.path(url.queryPath());
        // wrapper http2 body
        ByteBuf data = Unpooled.wrappedBuffer(request.body());
        return toHttp2StreamFrames(http2Headers, data);
    }

    /**
     * Converts to netty's http2 stream frames.
     *
     * @param response
     * @return
     */
    public static Http2StreamFrame[] toHttp2StreamFrames(HttpResponse<byte[]> response) {
        // build http2 headers
        Http2Headers http2Headers = toHttp2Headers(response.headers());
        http2Headers.status(HttpResponseStatus.valueOf(response.statusCode()).codeAsText());
        // wrapper http2 body
        ByteBuf data = Unpooled.wrappedBuffer(response.body());
        return toHttp2StreamFrames(http2Headers, data);
    }

    /**
     * Converts from netty's http2 stream.
     *
     * @param responseStream
     * @param context
     * @return
     */
    public static HttpResponse<ByteBuf> fromHttp2ResponseStream(Http2ResponseStream responseStream, InvocationContext<Envelope.Request, Caller<?>> context) {
        HttpCaller<?> httpCaller = (HttpCaller<?>) context.invoker();
        return HttpResponse.builder()
                .version(HttpVersion.HTTP_2_0)
                .method(httpCaller.httpMethod())
                .statusCode(responseStream.statusCode())
                .url(context.source().url())
                .headers(fromHttp2Headers(responseStream.headers()))
                .body(responseStream.body())
                .build();
    }

    /**
     * Converts from netty's http2 stream.
     *
     * @param requestStream
     * @return
     */
    public static HttpRequest<ByteBuf> fromHtt2RequestStream(Http2RequestStream requestStream) {
        return HttpRequest.builder()
                .version(HttpVersion.HTTP_2_0)
                .method(requestStream.method())
                .url(requestStream.url())
                .headers(fromHttp2Headers(requestStream.headers()))
                .body(requestStream.body())
                .build();
    }

    /**
     * Converts to netty's http2 headers.
     *
     * @param headers
     * @return
     */
    public static Http2Headers toHttp2Headers(HttpHeaders headers) {
        DefaultHttp2Headers httpHeaders = new DefaultHttp2Headers();
        headers.forEach(item -> httpHeaders.add(item.getKey().toString(), item.getValue().toString()));
        return httpHeaders;
    }

    /**
     * Converts from netty's http2 headers.
     *
     * @param headers
     * @return
     */
    public static HttpHeaders fromHttp2Headers(Http2Headers headers) {
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        headers.forEach(item -> httpHeaders.add(item.getKey().toString(), item.getValue().toString()));
        return httpHeaders;
    }

    private static <T extends Http2MessageStream> T acquireStream(String streamKey, ChannelHandlerContext ctx, Supplier<T> creator) {
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

    /**
     * Builds http2 settings.
     *
     * @param url
     * @return
     */
    public static Http2Settings buildHttp2Settings(URL url) {
        int initialWindows = url.getIntParam(DefaultConfigKeys.INITIAL_WINDOW_SIZE.key(),
                Constant.DEFAULT_INITIAL_WINDOW_SIZE);
        int maxConcurrentStreams = url.getIntParam(DefaultConfigKeys.MAX_CONCURRENT_STREAMS.key(),
                Constant.DEFAULT_MAX_CONCURRENT_STREAMS);
        int maxFrameSize = url.getIntParam(DefaultConfigKeys.MAX_FRAME_SIZE.key(),
                Constant.DEFAULT_MAX_FRAME_SIZE);
        int maxHeaderListSize = url.getIntParam(DefaultConfigKeys.MAX_HEADER_LIST_SIZE.key(),
                Constant.DEFAULT_MAX_HEADER_LIST_SIZE);
        int headerTableSize = url.getIntParam(DefaultConfigKeys.HEADER_TABLE_SIZE.key(),
                Constant.DEFAULT_MAX_HEADER_TABLE_SIZE);
        Http2Settings settings = new Http2Settings();
        settings.initialWindowSize(initialWindows);
        settings.maxConcurrentStreams(maxConcurrentStreams);
        settings.maxFrameSize(maxFrameSize);
        settings.maxHeaderListSize(maxHeaderListSize);
        settings.headerTableSize(headerTableSize);
        if (URLType.CLIENT.match(url)) {
            boolean pushEnabled = url.getBooleanParam(DefaultConfigKeys.PUSH_ENABLED.key(), false);
            settings.pushEnabled(pushEnabled);
        }
        return settings;
    }

}
