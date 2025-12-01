package io.effi.rpc.config;

import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.trait.FluentBuilder;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.trait.Replicable;
import io.effi.rpc.util.StringUtil;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents URLs with protocol, address, path segments, and query parameters.
 * <p>
 * Provides comprehensive URL parsing, building, modification, and reconstruction
 * capabilities with support for path variables and query parameter management.
 */
public class SmartURL extends AbstractAttributes implements Replicable<SmartURL> {

    private final String scheme;

    private String host;

    private int port;

    private final QueryPath queryPath;

    private final Map<String, String> queryParams = new HashMap<>();

    SmartURL(String scheme, String host, int port, QueryPath queryPath, Map<String, String> queryParams) {
        this.scheme = AssertUtil.notBlank(scheme, "scheme");
        this.queryPath = queryPath;
        this.host = host;
        this.port = port;
        addQueryParams(queryParams);
    }

    /**
     * Parses a URL string into a {@link SmartURL} object.
     */
    public static SmartURL valueOf(String url) {
        AssertUtil.notBlank(url, "url");

        // Find the position of the question mark
        int questionMarkIndex = url.indexOf('?');
        String fixed = questionMarkIndex == -1 ? url : url.substring(0, questionMarkIndex);

        // Extract protocol
        int protocolStartIndex = fixed.lastIndexOf("://");
        if (protocolStartIndex == -1) {
            throw new IllegalArgumentException("Invalid URL: " + url);
        }

        String protocol = fixed.substring(0, protocolStartIndex);
        // Remove protocol from fixed
        fixed = fixed.substring(protocolStartIndex + 3);

        // Extract address and path
        String address = null;
        String path = null;

        int firstSlashIndex = fixed.indexOf('/');
        if (firstSlashIndex != -1) {
            address = fixed.substring(0, firstSlashIndex);
            path = fixed.substring(firstSlashIndex);
        } else {
            address = fixed;
        }
        // Process query parameters
        Map<String, String> params = null;
        if (questionMarkIndex != -1) {
            String paramsStr = url.substring(questionMarkIndex + 1);
            params = URLUtil.parseQueryParam(paramsStr);
        }

        return builder()
                .scheme(protocol)
                .address(address)
                .path(path)
                .queryParams(params)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public SmartURL addQueryParam(String name, String value) {
        this.queryParams.put(name, value);
        return this;
    }

    public SmartURL addQueryParams(Map<String, String> params) {
        if (CollectionUtil.isNotEmpty(params)) {
            this.queryParams.putAll(params);
        }
        return this;
    }

    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    public String getQueryParam(String name, String defaultValue) {
        return queryParams.getOrDefault(name, defaultValue);
    }

    public SmartURL removeQueryParam(String name) {
        this.queryParams.remove(name);
        return this;
    }

    public SmartURL address(InetSocketAddress address) {
        if (address != null) {
            this.host = address.getHostString();
            this.port = address.getPort();
        }
        return this;
    }

    public String scheme() {
        return scheme;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String address() {
        if (StringUtil.isBlank(host)) {
            return StringUtil.empty();
        }
        return host + ":" + port;
    }

    public String path() {
        return queryPath == null
                ? StringUtil.empty()
                : queryPath.path();
    }

    public String query() {
        return URLUtil.toQueryParam(queryParams);
    }

    public Map<String, String> queryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    public String queryPath() {
        String query = query();
        return path() + (StringUtil.isBlank(query) ? "" : ("?" + query));
    }

    public String origin() {
        return scheme + "://" + address();
    }

    public String baseUrl() {
        return origin() + "/" + path();
    }

    public String fullPath() {
        return origin() + "/" + queryPath();
    }


    @Override
    public SmartURL replicate() {
        //        config.accessor.putAll(this.accessor);
        return builder()
                .scheme(scheme)
                .host(host)
                .port(port)
                .path(queryPath)
                .queryParams(queryParams)
                .build();
    }

    @Override
    public String toString() {
        return fullPath();
    }

    /**
     * Supplies access to the {@link SmartURL}.
     */
    public interface Supplier {

        /**
         * Returns the associated {@link SmartURL}.
         */
        SmartURL url();

    }

    /**
     * Builds {@link SmartURL} instances.
     */
    public static class Builder implements FluentBuilder<SmartURL, Builder> {

        private String scheme;

        private String host;

        private int port;

        private QueryPath queryPath;

        private final Map<String, String> queryParams = new HashMap<>();

        Builder() {
        }

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder address(String address) {
            return address(NetUtil.toInetSocketAddress(address));
        }

        public Builder address(InetSocketAddress address) {
            if (address != null) {
                host(address.getHostString());
                port(address.getPort());
            }
            return this;
        }

        public Builder path(String path) {
            return path(QueryPath.valueOf(path));
        }

        public Builder path(QueryPath path) {
            this.queryPath = path;
            queryParams(path.queryParams());
            return this;
        }

        public Builder queryParams(Map<String, String> params) {
            if (CollectionUtil.isNotEmpty(params)) {
                this.queryParams.putAll(params);
            }
            return this;
        }

        @Override
        public SmartURL build() {
            return new SmartURL(scheme, host, port, queryPath, queryParams);
        }
    }

}
