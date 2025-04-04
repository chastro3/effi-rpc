package io.effi.rpc.common.url;

import io.effi.rpc.common.util.AbstractAttributes;
import io.effi.rpc.common.util.Replicable;
import io.effi.rpc.common.util.collection.LazyList;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.NetUtil;
import io.effi.rpc.common.util.StringUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * URL with its various components, including protocol, address,
 * path segments, and query parameters. This class provides methods for parsing
 * a URL string into its components, adding or removing path segments and
 * query parameters, and constructing the complete URL representation.
 *
 * <p>The URL class implements the {@link Replicable} interface, allowing for
 * the creation of deep copies of URL instances. It also extends
 * {@link AbstractAttributes} to engine attribute management.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * URL url = URL.valueOf("http://www.example.com/path/to/resource?param1=value1");
 * url.addParam("param2", "value2");
 * String fullUrl = url.uri();
 * </pre>
 */
public class URL extends AbstractAttributes implements Replicable<URL> {

    private final List<String> paths = new LazyList<>(ArrayList::new);

    private final Config params = new Config();

    protected URLType type = URLType.DEFAULT;

    protected String protocol;

    protected String address;

    protected String host;

    protected int port;

    public URL(URLType type, String protocol, String address, List<String> paths, Map<String, String> params) {
        AssertUtil.notBlank(protocol, "protocol");
        AssertUtil.notBlank(address, "address");
        if (type != null) this.type = type;
        protocol(protocol);
        address(address);
        paths(paths);
        addParams(params);
    }

    /**
     * Parses the given URL string and creates a corresponding URL object.
     *
     * @param url the URL string to parameter
     * @return the URL object
     * @throws IllegalArgumentException if the URL is blank or invalid
     */
    public static URL valueOf(String url) {
        if (StringUtil.isBlank(url)) {
            throw new IllegalArgumentException("Url is blank");
        }

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
                .protocol(protocol)
                .address(address)
                .path(path)
                .params(params)
                .build();
    }

    /**
     * Creates a new URLBuilder instance for constructing a URL.
     *
     * @return a new URLBuilder
     */
    public static URLBuilder builder() {
        return new URLBuilder();
    }

    /**
     * Sets the protocol for the URL.
     *
     * @param protocol
     * @return
     */
    public URL protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Sets the address and extracts the host and port if valid.
     *
     * @param address the address to set
     * @return the current URL instance
     */
    public URL address(String address) {
        InetSocketAddress socketAddress = NetUtil.validateAddress(address);
        if (socketAddress != null) {
            address(socketAddress);
        } else {
            this.address = address;
        }
        return this;
    }

    public URL address(InetSocketAddress address) {
        if (address != null) {
            this.host = address.getHostString();
            this.port = address.getPort();
            this.address = NetUtil.toAddress(host, port);
        }
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
    public URL path(String path) {
        QueryPath queryPath = QueryPath.valueOf(path);
        paths.clear();
        paths.addAll(queryPath.paths());
        addParams(queryPath.queryParams());
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
    public URL paths(List<String> paths) {
        if (CollectionUtil.isNotEmpty(paths)) {
            this.paths.clear();
            this.paths.addAll(paths);
        }
        return this;
    }

    /**
     * Retrieves the path segment at the specified index.
     *
     * @param index the index of the path segment to retrieve
     * @return the path segment, or null if the index is out of bounds
     */
    public String getPath(int index) {
        if (paths.size() > index) {
            return paths.get(index);
        }
        return null;
    }

    /**
     * Adds a query parameter to the URL.
     *
     * @param key   the parameter key
     * @param value the parameter value
     * @return the current URL instance
     */
    public URL addParam(String key, String value) {
        params.set(key, value);
        return this;
    }

    /**
     * Adds multiple query parameters to the URL.
     *
     * @param params the map of parameters to add
     * @return the current URL instance
     */
    public URL addParams(Map<String, String> params) {
        if (CollectionUtil.isNotEmpty(params)) {
            this.params.set(params);
        }
        return this;
    }

    /**
     * Retrieves the value of a query parameter by its key.
     *
     * @param key the parameter key
     * @return the parameter value, or null if not found
     */
    public String getParam(String key) {
        return params.get(key);
    }

    /**
     * Retrieves the value of a query parameter by its key, returning a default value if not found.
     *
     * @param key          the parameter key
     * @param defaultValue the default value to return if not found
     * @return the parameter value, or the default value if not found
     */
    public String getParam(String key, String defaultValue) {
        return params.get(key, defaultValue);
    }

    /**
     * Retrieves a boolean value for a query parameter.
     *
     * @param key the parameter key
     * @return the boolean value, or false if the parameter is not found or not a valid boolean
     */
    public boolean getBooleanParam(String key) {
        return Boolean.parseBoolean(getParam(key));
    }

    /**
     * Retrieves a boolean value for a query parameter, returning a default value if not found.
     *
     * @param key          the parameter key
     * @param defaultValue the default value to return if not found
     * @return the boolean value, or the default value if not found
     */
    public boolean getBooleanParam(String key, boolean defaultValue) {
        String value = getParam(key);
        return StringUtil.isBlank(value) ? defaultValue : Boolean.parseBoolean(value);
    }

    /**
     * Retrieves an integer value for a query parameter.
     *
     * @param key the parameter key
     * @return the integer value
     * @throws NumberFormatException if the value is not a valid integer
     */
    public int getIntParam(String key) {
        return Integer.parseInt(getParam(key));
    }

    /**
     * Retrieves an integer value for a query parameter, returning a default value if not found.
     *
     * @param key          the parameter key
     * @param defaultValue the default value to return if not found
     * @return the integer value, or the default value if not found
     */
    public int getIntParam(String key, int defaultValue) {
        String value = getParam(key);
        return StringUtil.isBlank(value) ? defaultValue : Integer.parseInt(value);
    }

    /**
     * Retrieves a long value for a query parameter.
     *
     * @param key the parameter key
     * @return the long value
     * @throws NumberFormatException if the value is not a valid long
     */
    public long getLongParam(String key) {
        return Long.parseLong(getParam(key));
    }

    /**
     * Retrieves a long value for a query parameter, returning a default value if not found.
     *
     * @param key          the parameter key
     * @param defaultValue the default value to return if not found
     * @return the long value, or the default value if not found
     */
    public long getLongParam(String key, long defaultValue) {
        String value = getParam(key);
        return StringUtil.isBlank(value) ? defaultValue : Long.parseLong(value);
    }

    /**
     * Removes a query parameter by its key.
     *
     * @param key the parameter key
     * @return the current URL instance
     */
    public URL removeParam(String key) {
        params.remove(key);
        return this;
    }

    /**
     * Returns the URL type.
     *
     * @return
     */
    public URLType type() {
        return type;
    }

    /**
     * Returns the protocol.
     *
     * @return
     */
    public String protocol() {
        return protocol;
    }

    /**
     * Returns the address.
     *
     * @return
     */
    public String address() {
        return address;
    }

    /**
     * Returns the host.
     *
     * @return
     */
    public String host() {
        return host;
    }

    /**
     * Returns the port.
     *
     * @return
     */
    public int port() {
        return port;
    }

    /**
     * Returns the list of path segments.
     *
     * @return
     */
    public List<String> paths() {
        return Collections.unmodifiableList(paths);
    }

    /**
     * Returns the map of query parameters.
     *
     * @return
     */
    public Config params() {
        return params;
    }

    /**
     * Constructs the authority string of the URL.
     *
     * @return the authority string in the format protocol://address
     */
    public String authority() {
        return protocol + "://" + address;
    }

    /**
     * Constructs the complete URI string of the URL.
     *
     * @return the complete URI string
     */
    public String uri() {
        return authority() + path();
    }

    /**
     * Constructs the path string from the list of path segments.
     *
     * @return the constructed path string
     */
    public String path() {
        return URLUtil.toPath(paths);
    }

    /**
     * Constructs the query path string from the URL's parameters.
     *
     * @return the query path string in the format /path?param=value
     */
    public String queryPath() {
        String queryParam = URLUtil.toQueryParam(params.properties());
        return path() + (StringUtil.isBlank(queryParam) ? "" : ("?" + queryParam));
    }

    /**
     * Returns a string representation of the URL in the format protocol://address/path?param=value.
     *
     * @return the string representation of the URL
     */
    @Override
    public String toString() {
        return authority() + queryPath();
    }

    /**
     * Creates a deep copy of the URL instance.
     *
     * @return a new URL instance that is a copy of the current instance
     */
    @Override
    public URL replicate() {
//        url.accessor.putAll(this.accessor);
        return builder()
                .type(type)
                .protocol(protocol)
                .address(address)
                .params(params.properties())
                .paths(paths)
                .build();
    }

}
