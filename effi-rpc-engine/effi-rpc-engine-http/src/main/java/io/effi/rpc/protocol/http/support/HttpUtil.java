package io.effi.rpc.protocol.http.support;

import io.effi.rpc.common.config.*;
import io.effi.rpc.common.constant.EffiRpcFramework;
import io.effi.rpc.common.spi.ExtensionLoader;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.Callee;
import io.effi.rpc.contract.annotation.Body;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.serialization.Serializer;
import io.effi.rpc.transport.netty.NettySupport;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.util.AsciiString;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.effi.rpc.common.constant.Component.Compression.*;
import static io.effi.rpc.common.constant.Component.Protocol.*;

/**
 * Utility class for handling HTTP-related operations and transformations.
 */
public final class HttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private static final String IDENTIFY = "effi-rpc/" + EffiRpcFramework.version();

    private static final String ACCEPT_TYPE = String.join(",", Arrays.stream(MediaType.values()).map(MediaType::getName).toList());

    /**
     * Creates common headers for client requests.
     *
     * @return A map of headers typically used in HTTP client requests.
     */
    public static Map<CharSequence, CharSequence> regularRequestHeaders() {
        String acceptEncoding = String.join(",", GZIP, DEFLATE, LZ4, SNAPPY);
        return Map.of(
                HttpHeaderNames.ACCEPT_ENCODING, acceptEncoding,
                HttpHeaderNames.ACCEPT, ACCEPT_TYPE,
                HttpHeaderNames.USER_AGENT, IDENTIFY
        );
    }

    /**
     * Creates common headers for server responses.
     *
     * @return A map of headers typically used in HTTP server responses.
     */
    public static Map<CharSequence, CharSequence> regularResponseHeaders() {
        return Map.of(
                HttpHeaderNames.SERVER, IDENTIFY
        );
    }

    public static Object getBody(HttpRequest<ByteBuf> request, Body body, Parameter parameter, Callee<?> callee) {
        byte[] bodyBytes = NettySupport.getBytes(request.body());
        CharSequence contentType = request.headers().get(HttpHeaderNames.CONTENT_TYPE.toString());
        Type type = parameter.getParameterizedType();
        return decodeBody(contentType, bodyBytes, type);
    }

    public static String findPathForVar(URL requestUrl, String pathVarKey, Callee<?> callee) {
        if (requestUrl == null || StringUtil.isBlank(pathVarKey)) {
            return null;
        }
        List<String> requestUrlPaths = requestUrl.paths();
        QueryPath queryPath = callee.queryPath();
        if (queryPath != null && CollectionUtil.isNotEmpty(queryPath.paths())) {
            List<String> paths = queryPath.paths();
            for (int i = 0; i < paths.size(); i++) {
                String calleeUrlPath = paths.get(i);
                String var = URLUtil.getVar(calleeUrlPath);
                if (StringUtil.isNotBlank(var) && Objects.equals(var, pathVarKey)) {
                    return requestUrlPaths.get(i);
                }
            }
        }
        return null;
    }

    public static String findParamForVar(URL requestUrl, String paramVarKey) {
        Map<String, String> requestParams = requestUrl.params().items();
        for (String paramKey : requestParams.keySet()) {
            if (Objects.equals(paramKey, paramVarKey)) {
                return requestParams.get(paramKey);
            }
        }
        return null;
    }

    /**
     * Sets the Content-Length header in the provided headers based on the body size.
     *
     * @param headers The headers to modify.
     * @param body    The body whose size is used to set the Content-Length.
     */
    public static void setContentLength(HttpHeaders headers, Object body) {
        if (body instanceof ByteBuf byteBuf) {
            headers.add(HttpHeaderNames.CONTENT_LENGTH, AsciiString.of(String.valueOf(byteBuf.readableBytes())));
        } else if (body instanceof byte[] bytes) {
            headers.add(HttpHeaderNames.CONTENT_LENGTH, AsciiString.of(String.valueOf(bytes.length)));
        }
    }

    /**
     * Sets the Content-Type header based on the provided URL parameters or existing header.
     *
     * @param headers The headers to modify.
     * @param config  The config used to determine the content type.
     */
    public static void setContentType(HttpHeaders headers, Config config) {
        CharSequence contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
        MediaType mediaType;
        if (StringUtil.isBlank(contentType)) {
            String serialization = config.get(DefaultConfigKeys.SERIALIZATION);
            mediaType = MediaType.fromSerialization(serialization);
            if (mediaType == null)
                throw new IllegalArgumentException("unsupported serialization ['" + serialization + "'] convert to MediaType");
        } else {
            mediaType = MediaType.fromName(contentType);
            if (mediaType == null)
                throw new IllegalArgumentException("unsupported contentType ['" + contentType + "'] convert to MediaType");
        }
        headers.add(HttpHeaderNames.CONTENT_TYPE, mediaType.getName());
    }

    /**
     * Encodes the body of the HTTP envelope into a byte array based on its content type.
     *
     * @param envelope The HTTP envelope containing the body to encode.
     * @return The encoded byte array of the body.
     */
    public static byte[] encodeBody(HttpEnvelope<?> envelope) {
        CharSequence charSequence = envelope.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (envelope instanceof HttpResponse<?> response
                && !response.isSuccess()
                && response.body() instanceof String bodyStr) {
            return bodyStr.getBytes(StandardCharsets.UTF_8);
        }
        return encodeBody(charSequence, envelope.body());
    }

    /**
     * Decodes the body of the HTTP request into the specified type.
     *
     * @param request  The HTTP request containing the body to decode.
     * @param bodyType The type to which the body should be decoded.
     * @return The decoded body as an object of the specified type.
     */
    public static Object decodeBody(HttpRequest<ByteBuf> request, Type bodyType) {
        return decodeBody(
                request.headers().get(HttpHeaderNames.CONTENT_TYPE.toString()),
                NettySupport.getBytes(request.body()),
                bodyType
        );
    }

    /**
     * Decodes the body of the HTTP response into the specified type.
     *
     * @param response The HTTP response containing the body to decode.
     * @param bodyType The type to which the body should be decoded.
     * @return The decoded body as an object of the specified type, or the body as a string if the status code is not 200.
     */
    public static Object decodeBody(HttpResponse<ByteBuf> response, Type bodyType) {
        byte[] bodyBytes = NettySupport.getBytes(response.body());
        if (response.isSuccess()) {
            return decodeBody(response.headers().get(HttpHeaderNames.CONTENT_TYPE.toString()), bodyBytes, bodyType);
        } else {
            return new String(bodyBytes, StandardCharsets.UTF_8);
        }
    }

    /**
     * Decodes the body of the HTTP message into the specified type.
     *
     * @param contentType
     * @param bytes
     * @param bodyType
     * @return
     */
    public static Object decodeBody(CharSequence contentType, byte[] bytes, Type bodyType) {
        Serializer serializer = getSerializer(contentType);
        return serializer.deserialize(bytes, bodyType);
    }

    /**
     * Converts the given config protocol to a standard scheme.
     *
     * @param url the URL to convert
     * @return the standard scheme as a String
     * @throws UnsupportedOperationException if the protocol is unsupported
     */
    public static String toStandardScheme(URL url) {
        return switch (url.protocol()) {
            case HTTP, H2C -> HTTP;
            case HTTPS, H2 -> HTTPS;
            default -> throw new UnsupportedOperationException("Unsupported protocol: " + url.protocol());
        };
    }

    /**
     * Encodes the body into a byte array based on its content type.
     *
     * @param contentType The content type used to determine the serialization method.
     * @param body        The body to encode.
     * @return The encoded byte array of the body.
     */
    public static byte[] encodeBody(CharSequence contentType, Object body) {
        if (body == null) {
            return new byte[0];
        }
        return getSerializer(contentType).serialize(body);
    }

    /**
     * Retrieves the appropriate serializer based on the given content type.
     *
     * @param contentType The content type used to load the serializer.
     * @return The serializer corresponding to the content type.
     * @throws UnsupportedOperationException if the content type is not supported.
     */
    private static Serializer getSerializer(CharSequence contentType) {
        MediaType mediaType = MediaType.fromName(contentType);
        if (mediaType == null) {
            throw new UnsupportedOperationException("Unsupported content type: " + contentType + "'s serialization");
        }
        return ExtensionLoader.loadExtension(Serializer.class, mediaType.getSerialization());
    }

}

