package io.effi.rpc.config;

import io.effi.rpc.util.Builder;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds {@link URL} instances.
 */
public class URLBuilder implements Builder<URL> {

    private final Map<String, String> params = new HashMap<>();

    private URLType type;

    private String protocol;

    private String address;

    private List<String> paths;

    URLBuilder() {
    }

    public URLBuilder type(URLType type) {
        this.type = type;
        return this;
    }

    public URLBuilder protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public URLBuilder address(InetSocketAddress address) {
        return address(NetUtil.toAddress(address));
    }

    public URLBuilder address(String address) {
        this.address = address;
        return this;
    }

    public URLBuilder path(String path) {
        QueryPath queryPath = QueryPath.valueOf(path);
        paths = queryPath.paths();
        addParams(queryPath.queryParams());
        return this;
    }

    public URLBuilder params(Map<String, String> params) {
        addParams(params);
        return this;
    }

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