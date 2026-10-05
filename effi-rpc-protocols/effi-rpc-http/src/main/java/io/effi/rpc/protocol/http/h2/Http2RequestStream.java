package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.transport.netty.NettyChannel;
import io.effi.rpc.transport.netty.NettySupport;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.Http2FrameStream;

/**
 * Represents an aggregated HTTP/2 request stream.
 */
public class Http2RequestStream extends Http2MessageStream {

    private final NettyChannel channel;

    private SmartURL requestSmartUrl;

    public Http2RequestStream(NettyChannel channel, Http2FrameStream stream, int maxMessageSize) {
        super(stream, maxMessageSize);
        this.channel = channel;
    }

    /**
     * Returns the current request method.
     *
     * @return
     */
    public HttpMethod method() {
        CharSequence method = headers.method();
        if (method == null) {
            throw TransportErrorCodes.DECODE.fail(HttpMethod.class, "missing ':method' pseudo-header");
        }
        try {
            return HttpMethod.valueOf(method.toString());
        } catch (IllegalArgumentException e) {
            throw TransportErrorCodes.DECODE.fail(e, HttpMethod.class, method);
        }
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
        if (path == null) {
            throw TransportErrorCodes.DECODE.fail(SmartURL.class, "missing ':path' pseudo-header");
        }
        requestSmartUrl = NettySupport.createRequestUrl(channel, path.toString());
    }

}
