package io.effi.rpc.config;

import io.effi.rpc.trait.FluentBuilder;
import io.effi.rpc.trait.Replicable;
import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.NetUtil;
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
    private final QueryPath queryPath;
    private final Map<String, String> queryParams = new HashMap<>();
    private String host;
    private int port;

    SmartURL(String scheme, String host, int port, QueryPath queryPath, Map<String, String> queryParams) {
        this.scheme = AssertUtil.notBlank(scheme, "scheme");
        this.queryPath = queryPath;
        this.host = host;
        this.port = port;
        addQueryParams(queryParams);
    }

    /**
     * Adds or replaces query parameters.
     *
     * @param params query parameters
     * @return this URL
     */
    public SmartURL addQueryParams(Map<String, String> params) {
        if (CollectionUtil.isNotEmpty(params)) {
            this.queryParams.putAll(params);
        }
        return this;
    }

    /**
     * Parses a URL string into a {@link SmartURL} object.
     *
     * @param url URL string
     * @return parsed URL
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

    /**
     * Returns a new URL builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Adds or replaces a query parameter.
     *
     * @param name  parameter name
     * @param value parameter value
     * @return this URL
     */
    public SmartURL addQueryParam(String name, String value) {
        this.queryParams.put(name, value);
        return this;
    }

    /**
     * Returns a query parameter value.
     *
     * @param name parameter name
     * @return parameter value, or {@code null} when absent
     */
    public String getQueryParam(String name) {
        return queryParams.get(name);
    }

    /**
     * Returns a query parameter value or the supplied default.
     *
     * @param name         parameter name
     * @param defaultValue fallback value
     * @return parameter value, or the default when absent
     */
    public String getQueryParam(String name, String defaultValue) {
        return queryParams.getOrDefault(name, defaultValue);
    }

    /**
     * Removes a query parameter.
     *
     * @param name parameter name
     * @return this URL
     */
    public SmartURL removeQueryParam(String name) {
        this.queryParams.remove(name);
        return this;
    }

    /**
     * Sets the host and port from an address.
     *
     * @param address socket address
     * @return this URL
     */
    public SmartURL address(InetSocketAddress address) {
        if (address != null) {
            this.host = address.getHostString();
            this.port = address.getPort();
        }
        return this;
    }

    /**
     * Returns the URL scheme.
     */
    public String scheme() {
        return scheme;
    }

    /**
     * Returns the URL host.
     */
    public String host() {
        return host;
    }

    /**
     * Returns the URL port.
     */
    public int port() {
        return port;
    }

    /**
     * Returns the query parameters.
     */
    public Map<String, String> queryParams() {
        return Collections.unmodifiableMap(queryParams);
    }

    /**
     * Returns the origin and path.
     */
    public String baseUrl() {
        return origin() + "/" + path();
    }

    /**
     * Returns the scheme and authority.
     */
    public String origin() {
        return scheme + "://" + address();
    }

    /**
     * Returns the URL path.
     */
    public String path() {
        return queryPath == null
                ? StringUtil.empty()
                : queryPath.path();
    }

    /**
     * Returns the host and port.
     */
    public String address() {
        if (StringUtil.isBlank(host)) {
            return StringUtil.empty();
        }
        return host + ":" + port;
    }

    @Override
    public SmartURL replicate() {
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
     * Returns the origin, path, and query string.
     */
    public String fullPath() {
        return origin() + "/" + queryPath();
    }

    /**
     * Returns the path and encoded query string.
     */
    public String queryPath() {
        String query = query();
        return path() + (StringUtil.isBlank(query) ? "" : ("?" + query));
    }

    /**
     * Returns the encoded query string.
     */
    public String query() {
        return URLUtil.toQueryParam(queryParams);
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

        private final Map<String, String> queryParams = new HashMap<>();
        private String scheme;
        private String host;
        private int port;
        private QueryPath queryPath;

        Builder() {
        }

        /**
         * Sets the URL scheme.
         *
         * @param scheme URL scheme
         * @return this builder
         */
        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        /**
         * Sets the host and port from an address string.
         *
         * @param address address string
         * @return this builder
         */
        public Builder address(String address) {
            return address(NetUtil.toInetSocketAddress(address));
        }

        /**
         * Sets the host and port from a socket address.
         *
         * @param address socket address
         * @return this builder
         */
        public Builder address(InetSocketAddress address) {
            if (address != null) {
                host(address.getHostString());
                port(address.getPort());
            }
            return this;
        }

        /**
         * Sets the URL host.
         *
         * @param host URL host
         * @return this builder
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the URL port.
         *
         * @param port URL port
         * @return this builder
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Sets the URL path from a string.
         *
         * @param path URL path
         * @return this builder
         */
        public Builder path(String path) {
            return path(QueryPath.valueOf(path));
        }

        /**
         * Sets the URL path.
         *
         * @param path URL path
         * @return this builder
         */
        public Builder path(QueryPath path) {
            this.queryPath = path;
            queryParams(path.queryParams());
            return this;
        }

        /**
         * Adds query parameters.
         *
         * @param params query parameters
         * @return this builder
         */
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
