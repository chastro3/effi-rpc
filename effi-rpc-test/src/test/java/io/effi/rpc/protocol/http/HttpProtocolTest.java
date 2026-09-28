package io.effi.rpc.protocol.http;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpProtocolTest {

    @Test
    void errorResponseKeepsRequestMethod() {
        HttpDuplexRequest request = HttpDuplexRequest.builder()
                .version(Http1Protocol.VERSION)
                .method(HttpMethod.POST)
                .url(SmartURL.valueOf("http://127.0.0.1:8080/hello"))
                .headers(List.of())
                .build();

        HttpResponse response = (HttpResponse) new Http1Protocol().createErrorResponse(
                request,
                InteractionErrorCodes.SERVANT_NOT_FOUND.fail("/hello", "127.0.0.1:8080")
        );

        assertEquals(HttpMethod.POST, response.method());
        assertEquals(404, response.statusCode());
    }
}
