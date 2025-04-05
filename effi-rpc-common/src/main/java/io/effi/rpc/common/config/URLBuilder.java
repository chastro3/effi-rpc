package io.effi.rpc.common.config;

import io.effi.rpc.common.util.Builder;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder class for constructing instances of {@link URL}.
 * The URLBuilder provides a fluent API for setting the various components
 * of a URL, including the protocol, address, path segments, and query parameters.
 *
 * <p>This class follows the Builder design pattern, enabling easy and
 * readable construction of URL objects without requiring a complex
 * constructor. The resulting URL object can be built using the
 * {@link #build()} method.</p>
 */
public class URLBuilder implements Builder<URL> {

    private final Map<String, String> params = new HashMap<>();

    private URLType type;

    private String protocol;

    private String address;

    private List<String> paths;

    URLBuilder() {}

    /**
     * Sets the type of URL being built.
     *
     * @param type
     * @return
     */
    public URLBuilder type(URLType type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the protocol for the URL being built.
     *
     * @param protocol The protocol to set (e.g., "http", "https","custom").
     * @return This URLBuilder instance for method chaining.
     */
    public URLBuilder protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public URLBuilder address(InetSocketAddress address) {
        return address(NetUtil.toAddress(address));
    }

    /**
     * Sets the address for the URL being built.
     *
     * @param address The address to set (e.g., "www.example.com").
     * @return This URLBuilder instance for method chaining.
     */
    public URLBuilder address(String address) {
        this.address = address;
        return this;
    }

    /**
     * Accepts a query path string (e.g., "/path1/path2?param1=value1")
     * and converts it into standard path segments and query parameters.
     * The path segments and query parameters are then set in the
     * URL being built.
     *
     * @param path The query path string to process.
     * @return This URLBuilder instance for method chaining.
     */
    public URLBuilder path(String path) {
        QueryPath queryPath = QueryPath.valueOf(path);
        paths = queryPath.paths();
        addParams(queryPath.queryParams());
        return this;
    }

    /**
     * Sets multiple query parameters for the URL being built.
     *
     * @param params A map of query parameters to add.
     * @return This URLBuilder instance for method chaining.
     */
    public URLBuilder params(Map<String, String> params) {
        addParams(params);
        return this;
    }

    /**
     * Sets the path segments for the URL being built directly,
     * allowing the user to provide a list of standard path segments
     * and reducing the need for parsing.
     *
     * @param paths A list of path segments to set.
     * @return This URLBuilder instance for method chaining.
     */
    public URLBuilder paths(List<String> paths) {
        this.paths = paths;
        return this;
    }

    private void addParams(Map<String, String> params) {
        if (CollectionUtil.isNotEmpty(params)) {
            this.params.putAll(params);
        }
    }

    @Override
    public URL build() {
        return new URL(type, protocol, address, paths, params);
    }
}