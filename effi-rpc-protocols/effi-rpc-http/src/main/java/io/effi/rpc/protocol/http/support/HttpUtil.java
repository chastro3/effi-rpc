package io.effi.rpc.protocol.http.support;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.serialization.options.CompressionOptions;
import io.effi.rpc.component.serialization.options.SerializationOptions;
import io.effi.rpc.compression.Compressor;
import io.effi.rpc.constant.EffiRpcFramework;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.marshalling.MarshallingErrorCodes;
import io.effi.rpc.option.Options;
import io.effi.rpc.serialization.CompressibleSerializer;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.util.FileUtil;
import io.effi.rpc.util.StringUtil;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Provides HTTP header, body, and negotiation utilities.
 */
public final class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final String IDENTIFY = "effi-rpc/" + EffiRpcFramework.version();

    private HttpUtil() {
    }

    /**
     * Adds common negotiation headers for client requests.
     */
    public static void addRegularRequestHeaders(HttpHeaders headers, ScopedPlatform platform) {
        String acceptEncoding = acceptEncodings(platform);
        if (StringUtil.isNotBlank(acceptEncoding)) {
            headers.add(HttpHeaderNames.ACCEPT_ENCODING, acceptEncoding);
        }
        String acceptType = acceptTypes(platform);
        if (StringUtil.isNotBlank(acceptType)) {
            headers.add(HttpHeaderNames.ACCEPT, acceptType);
        }
        headers.add(HttpHeaderNames.USER_AGENT, IDENTIFY);
    }

    private static String acceptEncodings(ScopedPlatform platform) {
        return String.join(",", platform.namedExtensions(Compressor.class).keySet());
    }

    private static String acceptTypes(ScopedPlatform platform) {
        var serializerNames = platform.namedExtensions(Serializer.class).keySet();
        List<String> contentTypes = Arrays.stream(MediaType.values())
                .filter(mediaType -> serializerNames.contains(mediaType.serialization()))
                .map(mediaType -> mediaType.contentType().toString())
                .toList();
        return String.join(",", contentTypes);
    }

    /**
     * Creates common headers for server responses.
     */
    public static Map<CharSequence, CharSequence> regularResponseHeaders() {
        return Map.of(
                HttpHeaderNames.SERVER, IDENTIFY
        );
    }

    /**
     * Adds the Content-Type header based on the provided URL parameters or existing header.
     *
     * @param headers the headers to modify.
     * @param config  the config used to determine the content type.
     */
    public static void addContentType(HttpHeaders headers, Options options) {
        CharSequence contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        MediaType mediaType;
        if (StringUtil.isBlank(contentType)) {
            String serialization = options.option(SerializationOptions.SERIALIZER);
            mediaType = MediaType.fromSerialization(serialization);
            if (mediaType == null)
                throw new IllegalArgumentException("Unsupported serialization ['" + serialization + "'] convert to MediaType");
        } else {
            mediaType = MediaType.fromName(contentType);
            if (mediaType == null)
                throw new IllegalArgumentException("Unsupported contentType ['" + contentType + "'] convert to MediaType");
        }
        headers.add(HttpHeaderNames.CONTENT_TYPE, mediaType.contentType());
    }

    /**
     * Adds the content encoding header when the options declare a compressor.
     *
     * @param headers target headers
     * @param options peer options
     */
    public static void addContentEncoding(HttpHeaders headers, Options options) {
        addContentEncoding(headers, options, null);
    }

    /**
     * Adds the content encoding header when the options declare a compressor accepted by the
     * request.
     *
     * @param headers target headers
     * @param options peer options
     * @param request request carrying the accept-encoding header
     */
    public static void addContentEncoding(HttpHeaders headers, Options options, HttpMessage request) {
        String compressor = options.option(CompressionOptions.COMPRESSOR);
        if (StringUtil.isNotBlank(compressor) && acceptsEncoding(request, compressor)) {
            headers.set(HttpHeaderNames.CONTENT_ENCODING, compressor);
        }
    }

    private static boolean acceptsEncoding(HttpMessage request, String encoding) {
        if (request == null) {
            return true;
        }
        CharSequence acceptEncoding = request.headers().get(HttpHeaderNames.ACCEPT_ENCODING);
        if (StringUtil.isBlank(acceptEncoding)) {
            return true;
        }
        for (String token : acceptEncoding.toString().split(",")) {
            String[] parts = token.trim().split(";");
            String name = parts[0].trim();
            if (!"*".equals(name) && !name.equalsIgnoreCase(encoding)) {
                continue;
            }
            double quality = 1D;
            for (int i = 1; i < parts.length; i++) {
                String parameter = parts[i].trim();
                if (parameter.startsWith("q=")) {
                    try {
                        quality = Double.parseDouble(parameter.substring(2));
                    } catch (NumberFormatException ignored) {
                        // Treat malformed quality values as the default.
                    }
                }
            }
            if (quality > 0D) {
                return true;
            }
        }
        return false;
    }

    /**
     * Encodes the body of the HTTP envelope into a byte array based on its content type.
     *
     * @param message the HTTP envelope containing the body to encode.
     * @return the encoded byte array of the body.
     */
    public static void encodeBody(ScopedPlatform platform, HttpMessage message, OutputStream out) throws IOException {
        if (message instanceof HttpResponse response
                && !response.succeeded()
                && response.body() instanceof String bodyStr) {
            out.write(bodyStr.getBytes(StandardCharsets.UTF_8));
            return;
        }
        CharSequence contentType = message.headers().get(HttpHeaderNames.CONTENT_TYPE);
        try {
            ensureSerializer(platform, contentType, contentEncoding(message)).serialize(message.body(), out);
        } catch (IOException e) {
            throw MarshallingErrorCodes.ENCODE.fail(e, message.body() == null ? "null" : message.body().getClass(), contentType);
        }
    }

    /**
     * Retrieves the appropriate serializer based on the given content type.
     *
     * @param contentType the content type used to load the serializer.
     * @return the serializer corresponding to the content type.
     * @throws UnsupportedOperationException if the content type is not supported.
     */
    private static Serializer ensureSerializer(ScopedPlatform platform, CharSequence contentType, CharSequence contentEncoding) throws IOException {
        MediaType mediaType = MediaType.fromName(contentType);
        if (mediaType == null) {
            throw new IOException("Unsupported content type: " + contentType + "'s serialization");
        }
        Serializer serializer = platform.namedExtension(Serializer.class, mediaType.serialization());
        if (StringUtil.isBlank(contentEncoding)) {
            return serializer;
        }
        return new CompressibleSerializer(serializer, resolveCompressor(platform, contentEncoding), platform.options());
    }

    private static CharSequence contentEncoding(HttpMessage message) {
        return message.headers().get(HttpHeaderNames.CONTENT_ENCODING);
    }

    private static Compressor resolveCompressor(ScopedPlatform platform, CharSequence contentEncoding) throws IOException {
        String name = contentEncoding.toString().trim().toLowerCase(Locale.ROOT);
        try {
            return platform.namedExtension(Compressor.class, name);
        } catch (RuntimeException e) {
            throw new IOException("Unsupported content encoding: " + contentEncoding, e);
        }
    }

    public static Object decodeBody(ScopedPlatform platform, HttpMessage message, InputStream in, Type bodyType) throws IOException {
        if (message instanceof HttpResponse response
                && !response.succeeded()) {
            return new String(FileUtil.toBytes(in), StandardCharsets.UTF_8);
        }
        CharSequence contentType = message.headers().get(HttpHeaderNames.CONTENT_TYPE);
        try {
            return ensureSerializer(platform, contentType, contentEncoding(message)).deserialize(in, bodyType);
        } catch (IOException e) {
            throw MarshallingErrorCodes.DECODE.fail(e, bodyType, contentType);
        }
    }

    public static Serializer serializer(ScopedPlatform platform, HttpMessage message) throws IOException {
        CharSequence contentType = message.headers().get(HttpHeaderNames.CONTENT_TYPE);
        return ensureSerializer(platform, contentType, contentEncoding(message));
    }

}

