package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class H2SupportTest {

    @Test
    void addsAuthorityToRequestHeaders() {
        HttpDuplexRequest request = HttpDuplexRequest.builder()
                .version(Http2Protocol.VERSION)
                .method(HttpMethod.POST)
                .url(SmartURL.valueOf("http://127.0.0.1:8080/hello"))
                .headers(Http2Protocol.VERSION.newHeaders())
                .build();

        Http2StreamFrame[] frames = H2Support.toHttp2StreamFrames(request);
        Http2HeadersFrame headersFrame = (Http2HeadersFrame) frames[0];

        assertEquals("127.0.0.1:8080", headersFrame.headers().authority().toString());
    }
}
