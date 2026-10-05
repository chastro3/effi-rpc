package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.transport.TransportErrorCodes;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http2.Http2FrameStream;

/**
 * Represents an aggregated HTTP/2 response stream.
 */
public class Http2ResponseStream extends Http2MessageStream {

    public Http2ResponseStream(Http2FrameStream stream, int maxMessageSize) {
        super(stream, maxMessageSize);
    }

    public int statusCode() {
        CharSequence status = headers.status();
        if (status == null) {
            throw TransportErrorCodes.DECODE.fail(HttpResponseStatus.class, "missing ':status' pseudo-header");
        }
        HttpResponseStatus responseStatus;
        try {
            responseStatus = HttpResponseStatus.parseLine(status);
        } catch (RuntimeException e) {
            throw TransportErrorCodes.DECODE.fail(e, HttpResponseStatus.class, status);
        }
        if (responseStatus == null) {
            throw TransportErrorCodes.DECODE.fail(HttpResponseStatus.class, status);
        }
        return responseStatus.code();
    }

}
