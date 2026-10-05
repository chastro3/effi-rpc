package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.protocol.http.support.HttpDuplexResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class Http2ServerHandlerTest {

    @Test
    void completesWritePromiseWhenWritingResponse() {
        EmbeddedChannel channel = new EmbeddedChannel(new Http2ServerHandler());
        HttpDuplexResponse response = HttpDuplexResponse.builder()
                .version(Http2Protocol.VERSION)
                .method(HttpMethod.POST)
                .statusCode(200)
                .url(SmartURL.valueOf("http://127.0.0.1:8080/hello"))
                .headers(Http2Protocol.VERSION.newHeaders())
                .build();

        ChannelFuture future = channel.writeAndFlush(response);

        assertTrue(future.isDone(), "HTTP/2 response write must complete");
        assertTrue(future.isSuccess(), "HTTP/2 response write must succeed");
        channel.finishAndReleaseAll();
    }
}
