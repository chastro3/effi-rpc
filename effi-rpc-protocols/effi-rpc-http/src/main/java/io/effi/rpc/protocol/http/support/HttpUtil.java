package io.effi.rpc.protocol.http.support;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.config.Options;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.constant.EffiRpcFramework;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Servant;
import io.effi.rpc.context.annotation.Body;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.transport.TransportErrorCodes;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.FileUtil;
import io.effi.rpc.util.StringUtil;
import io.netty.handler.codec.http.HttpHeaderNames;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * Utility class for handling HTTP-related operations and transformations.
 */
public final class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final String IDENTIFY = "effi-rpc/" + EffiRpcFramework.version();

    private static final String ACCEPT_TYPE = String.join(",", Arrays.stream(MediaType.values()).map(MediaType::contentType).toList());

    /**
     * Creates common headers for client requests.
     */
    public static Map<CharSequence, CharSequence> regularRequestHeaders() {
        // todo 优化
        String acceptEncoding = String.join(",", "GZIP, DEFLATE, LZ4, SNAPPY");
        return Map.of(
                HttpHeaderNames.ACCEPT_ENCODING, acceptEncoding,
                HttpHeaderNames.ACCEPT, ACCEPT_TYPE,
                HttpHeaderNames.USER_AGENT, IDENTIFY
        );
    }

    /**
     * Creates common headers for server responses.
     */
    public static Map<CharSequence, CharSequence> regularResponseHeaders() {
        return Map.of(
                HttpHeaderNames.SERVER, IDENTIFY
        );
    }

    public static Object getBody(HttpRequest request, Body body, Parameter parameter, Servant servant) {
        try {
            return decodeBody(servant.platform(), request, request.body(), parameter.getParameterizedType());
        } catch (IOException e) {
            throw TransportErrorCodes.DECODE.fail(e, request.getClass(), parameter.getType());
        }
    }

    public static String findPathForVar(SmartURL url, String pathVarName, Servant servant) {
        //todo 待完善
        if (url == null || StringUtil.isBlank(pathVarName)) {
            return null;
        }
        QueryPath queryPath = servant.queryPath();
        Map<String, String> varMap = queryPath.match(url.path());
        if (CollectionUtil.isNotEmpty(varMap)) {
            return varMap.get(pathVarName);
        }
        return null;
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
            String serialization = options.option(Peer.SERIALIZER);
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
        }
        CharSequence contentType = message.headers().get(HttpHeaderNames.CONTENT_TYPE);
        ensureSerializer(platform, contentType).serialize(message.body(), out);
    }

    public static Object decodeBody(ScopedPlatform platform, HttpMessage message, InputStream in, Type bodyType) throws IOException {
        if (message instanceof HttpResponse response
                && !response.succeeded()) {
            return new String(FileUtil.toBytes(in), StandardCharsets.UTF_8);
        }
        CharSequence contentType = message.headers().get(HttpHeaderNames.CONTENT_TYPE);
        return ensureSerializer(platform, contentType).deserialize(in, bodyType);
    }

    /**
     * Retrieves the appropriate serializer based on the given content type.
     *
     * @param contentType the content type used to load the serializer.
     * @return the serializer corresponding to the content type.
     * @throws UnsupportedOperationException if the content type is not supported.
     */
    private static Serializer ensureSerializer(ScopedPlatform platform, CharSequence contentType) throws IOException {
        MediaType mediaType = MediaType.fromName(contentType);
        if (mediaType == null) {
            throw new IOException("Unsupported content type: " + contentType + "'s serialization");
        }
        return platform.namedExtension(Serializer.class, mediaType.serialization());
    }

}

