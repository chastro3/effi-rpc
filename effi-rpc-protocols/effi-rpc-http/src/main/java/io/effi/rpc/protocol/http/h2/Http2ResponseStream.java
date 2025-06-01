package io.effi.rpc.protocol.http.h2;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2FrameStream;

/**
 * Generate full http2 response stream.
 */
public class Http2ResponseStream extends Http2MessageStream {

    public Http2ResponseStream(Http2FrameStream stream) {
        super(stream);
    }

    public int statusCode() {
        CharSequence status = headers.status();
        return HttpResponseStatus.parseLine(status).code();
    }

}
