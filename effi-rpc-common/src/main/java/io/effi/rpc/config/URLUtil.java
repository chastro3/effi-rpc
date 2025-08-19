package io.effi.rpc.config;

import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides url operations.
 */
public final class URLUtil {


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
        if (CollectionUtil.isEmpty(params)) return StringUtil.empty();
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
                    .append(value != null ? URLEncoder.encode(value, encoding) : StringUtil.empty()); // Treat null values as empty strings
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

