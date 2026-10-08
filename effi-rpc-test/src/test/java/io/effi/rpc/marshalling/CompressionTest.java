package io.effi.rpc.marshalling;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.serialization.options.CompressionOptions;
import io.effi.rpc.compression.Compressor;
import io.effi.rpc.compression.GzipCompressor;
import io.effi.rpc.compression.Lz4Compressor;
import io.effi.rpc.compression.SnappyCompressor;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.protocol.http.h1.Http1Protocol;
import io.effi.rpc.protocol.http.support.HttpDuplexRequest;
import io.effi.rpc.protocol.http.support.HttpHeaders;
import io.effi.rpc.protocol.http.support.HttpUtil;
import io.effi.rpc.protocol.http.support.MediaType;
import io.effi.rpc.serialization.CompressibleSerializer;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.serialization.json.JacksonSerializer;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompressionTest {

    @Test
    void compressibleSerializerRoundTripsJson() throws IOException {
        CompressibleSerializer serializer = new CompressibleSerializer(new JacksonSerializer(), new GzipCompressor());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize(Map.of("name", "tom"), out);

        Object value = serializer.deserialize(new ByteArrayInputStream(out.toByteArray()), Map.class);

        assertEquals(Map.of("name", "tom"), value);
    }

    @Test
    void lz4RoundTripsJson() throws IOException {
        assertCompressionRoundTrip(new Lz4Compressor());
    }

    private static void assertCompressionRoundTrip(Compressor compressor) throws IOException {
        CompressibleSerializer serializer = new CompressibleSerializer(new JacksonSerializer(), compressor);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize(Map.of("name", "tom"), out);

        Object value = serializer.deserialize(new ByteArrayInputStream(out.toByteArray()), Map.class);

        assertEquals(Map.of("name", "tom"), value);
    }

    @Test
    void snappyRoundTripsJson() throws IOException {
        assertCompressionRoundTrip(new SnappyCompressor());
    }

    @Test
    void compressibleSerializerRejectsOversizedPayload() throws IOException {
        CompressibleSerializer serializer = new CompressibleSerializer(
                new JacksonSerializer(),
                new GzipCompressor(),
                4
        );
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serializer.serialize(Map.of("name", "0123456789"), out);

        EffiRpcException failure = assertThrows(
                EffiRpcException.class,
                () -> serializer.deserialize(new ByteArrayInputStream(out.toByteArray()), Map.class)
        );

        assertEquals(MarshallingErrorCodes.DECOMPRESS, failure.errorCode());
    }

    @Test
    void addContentEncodingUsesConfiguredCompressor() {
        HierarchicalOptions options = HierarchicalOptions.create();
        options.addOption(CompressionOptions.COMPRESSOR, GzipCompressor.NAME);
        HttpHeaders headers = Http1Protocol.VERSION.newHeaders();

        HttpUtil.addContentEncoding(headers, options);

        assertEquals(GzipCompressor.NAME, headers.get(HttpHeaderNames.CONTENT_ENCODING).toString());
    }

    @Test
    void addContentEncodingRespectsAcceptEncoding() {
        HierarchicalOptions options = HierarchicalOptions.create();
        options.addOption(CompressionOptions.COMPRESSOR, GzipCompressor.NAME);
        HttpDuplexRequest request = request("deflate");
        HttpHeaders headers = Http1Protocol.VERSION.newHeaders();

        HttpUtil.addContentEncoding(headers, options, request);

        assertNull(headers.get(HttpHeaderNames.CONTENT_ENCODING));
    }

    private static HttpDuplexRequest request(String acceptEncoding) {
        HttpHeaders headers = Http1Protocol.VERSION.newHeaders();
        headers.set(HttpHeaderNames.ACCEPT_ENCODING, acceptEncoding);
        return HttpDuplexRequest.builder()
                .version(Http1Protocol.VERSION)
                .method(HttpMethod.POST)
                .url(SmartURL.builder().scheme(Http1Protocol.VERSION.name()).path("test").build())
                .headers(headers)
                .body(Map.of("name", "tom"))
                .build();
    }

    @Test
    void addContentEncodingAcceptsMatchingEncoding() {
        HierarchicalOptions options = HierarchicalOptions.create();
        options.addOption(CompressionOptions.COMPRESSOR, GzipCompressor.NAME);
        HttpDuplexRequest request = request("gzip;q=0.7, deflate");
        HttpHeaders headers = Http1Protocol.VERSION.newHeaders();

        HttpUtil.addContentEncoding(headers, options, request);

        assertEquals(GzipCompressor.NAME, headers.get(HttpHeaderNames.CONTENT_ENCODING).toString());
    }

    @Test
    void httpUtilCompressesAndDecompressesByContentEncodingHeader() throws IOException {
        ScopedPlatform platform = new ScopedPlatform("marshalling-compression-platform");
        platform.registry().register(Serializer.class, JacksonSerializer.NAME, new JacksonSerializer());
        platform.registry().register(Compressor.class, GzipCompressor.NAME, new GzipCompressor());
        try {
            HttpHeaders headers = Http1Protocol.VERSION.newHeaders();
            headers.set(HttpHeaderNames.CONTENT_TYPE, MediaType.APPLICATION_JSON.contentType());
            headers.set(HttpHeaderNames.CONTENT_ENCODING, GzipCompressor.NAME);
            HttpDuplexRequest request = HttpDuplexRequest.builder()
                    .version(Http1Protocol.VERSION)
                    .method(HttpMethod.POST)
                    .url(SmartURL.builder().scheme(Http1Protocol.VERSION.name()).path("test").build())
                    .headers(headers)
                    .body(Map.of("name", "tom"))
                    .build();
            ByteArrayOutputStream out = new ByteArrayOutputStream();

            HttpUtil.encodeBody(platform, request, out);

            HttpDuplexRequest input = HttpDuplexRequest.builder()
                    .version(Http1Protocol.VERSION)
                    .method(HttpMethod.POST)
                    .url(request.url())
                    .headers(headers)
                    .build()
                    .input(new ByteArrayInputStream(out.toByteArray()));
            Object value = HttpUtil.decodeBody(platform, input, input.inputStream(), Map.class);

            assertEquals(Map.of("name", "tom"), value);
        } finally {
            platform.close();
        }
    }
}
