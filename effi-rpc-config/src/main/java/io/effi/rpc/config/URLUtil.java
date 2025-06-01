package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides url operations.
 */
public final class URLUtil {

    /**
     * Extracts the variable from a string formatted as {variable}.
     */
    public static String getVar(String value) {
        int length = value.length();
        if (length > 1 && value.startsWith("{") && value.endsWith("}")) {
            return value.substring(1, length - 1);
        }
        return null;
    }

    /**
     * Converts a list of path segments into a single concatenated path string.
     */
    public static String toPath(List<String> paths) {
        if (CollectionUtil.isEmpty(paths)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String path : paths) {
            builder.append("/");
            builder.append(path);
        }
        return builder.toString();
    }

    /**
     * Splits a path string into individual segments.
     */
    public static List<String> toPaths(String path) {
        List<String> list = null;
        if (StringUtil.isBlank(path)) {
            return list;
        }
        String[] parts = path.split("/");
        if (CollectionUtil.isNotEmpty(parts)) {
            list = new ArrayList<>();
            for (String part : parts) {
                if (StringUtil.isNotBlank(part)) {
                    list.add(part);
                }
            }
        }
        return list;
    }

    /**
     * Parses a URL string, extracting the path and query parameters.
     */
    public static QueryPath buildQueryPath(String input) {
        String path = null;
        Map<String, String> params = null;
        if (StringUtil.isBlank(input)) {
            return new QueryPath(path, params);
        }
        int questionMarkIndex = input.indexOf('?');
        if (questionMarkIndex == -1) {
            // Treat the whole input as query parameters if it contains '='
            if (input.contains("=")) {
                params = parseQueryParam(input);
            } else {
                path = input;
            }
        } else {
            // Split into path and parameter parts
            path = input.substring(0, questionMarkIndex);
            String paramsPart = input.substring(questionMarkIndex + 1);
            if (!paramsPart.isEmpty()) {
                params = parseQueryParam(paramsPart);
            }
        }
        return new QueryPath(path, params);
    }

    /**
     * Converts a map of parameters into an encoded query string.
     */
    public static String toQueryParam(Map<String, String> params) {
        try {
            return toQueryParam(params, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding is not supported");
        }
    }

    /**
     * Converts a map of parameters into a query string with URL encoding using the specified encoding.
     */
    public static String toQueryParam(Map<String, String> params, String encoding) throws UnsupportedEncodingException {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringBuilder queryBuilder = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            // Skip null or empty keys
            if (key == null || key.isEmpty()) {
                continue;
            }
            if (!first) {
                queryBuilder.append('&');
            } else {
                first = false;
            }
            // URL encode both key and value
            queryBuilder.append(URLEncoder.encode(key, encoding))
                    .append('=')
                    .append(value != null ? URLEncoder.encode(value, encoding) : ""); // Treat null values as empty strings
        }
        return queryBuilder.toString();
    }

    /**
     * Parses query parameters from a URL string and returns them as a map.
     */
    public static Map<String, String> parseQueryParam(String paramsString) {
        try {
            return parseQueryParam(paramsString, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding is not supported");
        }
    }

    /**
     * Parses the query parameters from a string and returns them as a map.
     * Invalid formats are ignored without exceptions.
     */
    public static Map<String, String> parseQueryParam(String paramsString, String encoding) throws UnsupportedEncodingException {
        Map<String, String> params = new HashMap<>();
        int length = paramsString.length();
        int start = 0;

        while (start < length) {
            int equalsIndex = paramsString.indexOf('=', start);
            // If no '=' is found,
            String value;
            if (equalsIndex == -1 || equalsIndex == start) {
                break;
            }
            String key = paramsString.substring(start, equalsIndex);
            int ampersandIndex = paramsString.indexOf('&', equalsIndex + 1);

            if (ampersandIndex == -1) {
                // Last key-value pair
                value = paramsString.substring(equalsIndex + 1);
                start = length; // End the loop
            } else {
                value = paramsString.substring(equalsIndex + 1, ampersandIndex);
                start = ampersandIndex + 1;
            }
            // Only add valid keys to the map
            if (StringUtil.isNotBlank(key)) {
                params.put(URLDecoder.decode(key, encoding), URLDecoder.decode(value, encoding));
            }
        }
        return params.isEmpty() ? null : params;
    }

    private URLUtil() {
    }
}

