package io.effi.rpc.config;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.FluentBuilder;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.Replicable;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.collection.LazyList;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a URL with protocol, address, path segments, and query parameters.
 * Supports parsing, modification, and reconstruction.
 */
public class URL extends AbstractAttributes implements Replicable<URL>, ExtParams {

    private static final Map<String, String> PROTOCOL_MAPPING = loadProtocolMapping();

    private final List<String> paths = new LazyList<>(ArrayList::new);

    private final Config params = new FlatConfig(this);

    private URLType type = URLType.DEFAULT;

    private String standardProtocol;

    private String protocol;

    private String address;

    private String host;

    private int port;

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
     * Parses a URL string into a {@link URL} object.
     */
    public static URL valueOf(String url) {
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
                .protocol(protocol)
                .address(address)
                .path(path)
                .params(params)
                .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Sets the protocol.
     */
    public URL protocol(String protocol) {
        if (PROTOCOL_MAPPING != null) {
            String standardProtocol = PROTOCOL_MAPPING.get(protocol);
            if (standardProtocol != null) {
                this.protocol = protocol;
                this.standardProtocol = standardProtocol;
                return this;
            }
        }
        this.protocol = protocol;
        this.standardProtocol = protocol;
        return this;
    }

    /**
     * Sets the address and parses host and port if valid.
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
     * Sets path segments and query parameters from the given string.
     */
    public URL path(String path) {
        QueryPath queryPath = QueryPath.valueOf(path);
        paths.clear();
        paths.addAll(queryPath.paths());
        addParams(queryPath.queryParams());
        return this;
    }

    /**
     * Sets the path segments directly.
     */
    public URL paths(List<String> paths) {
        if (CollectionUtil.isNotEmpty(paths)) {
            this.paths.clear();
            this.paths.addAll(paths);
        }
        return this;
    }

    /**
     * Returns the path segment at the given index, or null if out of bounds.
     */
    public String getPath(int index) {
        if (paths.size() > index) {
            return paths.get(index);
        }
        return null;
    }

    public URLType type() {
        return type;
    }

    public String standardProtocol() {
        return standardProtocol;
    }

    public String protocol() {
        return protocol;
    }

    public String address() {
        return address;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public List<String> paths() {
        return Collections.unmodifiableList(paths);
    }

    public Config params() {
        return params;
    }

    public String authority() {
        return standardProtocol + "://" + address;
    }

    public String uri() {
        return authority() + path();
    }

    public String path() {
        return URLUtil.toPath(paths);
    }

    public String queryPath() {
        String queryParam = URLUtil.toQueryParam(params.items());
        return path() + (StringUtil.isBlank(queryParam) ? "" : ("?" + queryParam));
    }

    private static Map<String, String> loadProtocolMapping() {
        Collection<URLProtocolMapper> mappers = EffiRpcPlatform.getInstance()
                .extensionsOf(URLProtocolMapper.class);
        Map<String, String> map = new HashMap<>();
        if (CollectionUtil.isNotEmpty(mappers)) {
            map = new HashMap<>(mappers.size());
            for (URLProtocolMapper mapper : mappers) {
                map.put(mapper.supported(), mapper.mapped());
            }
        }
        return Collections.unmodifiableMap(map);
    }


    @Override
    public URL replicate() {
//        config.accessor.putAll(this.accessor);
        return builder()
                .type(type)
                .protocol(protocol)
                .address(address)
                .params(params.items())
                .paths(paths)
                .build();
    }

    @Override
    public Config config() {
        return params;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("(").append(protocol).append(") ");
        sb.append(authority()).append(queryPath());
        if (!attributes.isEmpty()) {
            sb.append(", ").append(super.toString());
        }
        return sb.toString();
    }

    /**
     * Provides access to the {@link URL}.
     */
    public interface Provider {

        /**
         * Returns the associated {@link URL}.
         */
        URL url();

    }

    /**
     * Builds {@link URL} instances.
     */
    public static class Builder implements FluentBuilder<URL, Builder> {

        private final Map<String, String> params = new HashMap<>();

        private URLType type;

        private String protocol;

        private String address;

        private List<String> paths;

        Builder() {
        }

        public Builder type(URLType type) {
            this.type = type;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder address(InetSocketAddress address) {
            return address(NetUtil.toAddress(address));
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder path(String path) {
            QueryPath queryPath = QueryPath.valueOf(path);
            paths = queryPath.paths();
            addParams(queryPath.queryParams());
            return this;
        }

        public Builder params(Map<String, String> params) {
            addParams(params);
            return this;
        }

        public Builder paths(List<String> paths) {
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

}
