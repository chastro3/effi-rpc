package io.effi.rpc.registry;

import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.registry.util.RegistryUtil;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.FluentBuilder;
import io.effi.rpc.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Provides the default implementation of {@link ServiceInstance}.
 */
public final class DefaultServiceInstance implements ServiceInstance {

    private final String id;

    private final String serviceName;

    private final String protocol;

    private final String host;

    private final int port;

    private final Map<String, String> metadata;

    private DefaultServiceInstance(Builder builder) {
        this.serviceName = AssertUtil.notBlank(builder.serviceName, "serviceName");
        this.protocol = AssertUtil.notBlank(builder.protocol, "protocol");
        this.host = AssertUtil.notBlank(builder.host, "host");
        this.port = builder.port;
        this.metadata = builder.metadata;
        this.id = safeGenerateId(builder.id);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String serviceName() {
        return serviceName;
    }

    @Override
    public String protocol() {
        return protocol;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public int port() {
        return port;
    }

    @Override
    public Map<String, String> metadata() {
        return metadata;
    }

    @Override
    public ServiceInstance addMetadata(String key, String value) {
        metadata.put(key, value);
        return this;
    }

    @Override
    public ServiceInstance addMetadata(Map<String, String> metadata) {
        if (CollectionUtil.isNotEmpty(metadata)) {
            this.metadata.putAll(metadata);
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceInstance that)) return false;
        return Objects.equals(id, that.id());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }

    private String safeGenerateId(String id) {
        return StringUtil.isNotBlank(id) ? id : RegistryUtil.generateId(protocol, host, port);
    }

    public static final class Builder implements FluentBuilder<DefaultServiceInstance, Builder> {

        private String id;

        private String serviceName;

        private String protocol;

        private String host;

        private int port;

        private final Map<String, String> metadata = new HashMap<>();

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder protocol(String protocol) {
            this.protocol = protocol;
            if (StringUtil.isNotBlank(protocol)) {
                metadata.put(KeyConstant.PROTOCOL, protocol);
            }
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

        public Builder addMetadata(Map<String, String> metadata) {
            if (CollectionUtil.isNotEmpty(metadata)) {
                this.metadata.putAll(metadata);
            }
            return this;
        }

        @Override
        public DefaultServiceInstance build() {
            return new DefaultServiceInstance(this);
        }
    }
}
