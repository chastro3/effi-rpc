package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettySupport;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.Http2FrameStream;

/**
 * Generate full http2 request stream.
 */
public class Http2RequestStream extends Http2MessageStream {

    private final NettyChannel channel;

    private SmartURL requestSmartUrl;

    public Http2RequestStream(NettyChannel channel, Http2FrameStream stream) {
        super(stream);
        this.channel = channel;
    }

    /**
     * Returns the current request method.
     *
     * @return
     */
    public HttpMethod method() {
        CharSequence method = headers.method();
        return HttpMethod.valueOf(method.toString());
    }

    /**
     * Returns the current request config.
     *
     * @return
     */
    public SmartURL url() {
        return requestSmartUrl;
    }

    @Override
    protected void end() {
        super.end();
        CharSequence path = headers.path();
        requestSmartUrl = NettySupport.createRequestUrl(channel, path.toString());
    }

}
