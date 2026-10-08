package io.effi.rpc.registry;

import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.registry.util.RegistryUtil;
import io.effi.rpc.trait.FluentBuilder;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
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

    private String safeGenerateId(String id) {
        return StringUtil.isNotBlank(id) ? id : RegistryUtil.generateId(protocol, host, port);
    }

    /**
     * Returns a new service instance builder.
     */
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
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceInstance that)) return false;
        return Objects.equals(id, that.id());
    }

    @Override
    public String toString() {
        return id;
    }

    public static final class Builder implements FluentBuilder<DefaultServiceInstance, Builder> {

        private final Map<String, String> metadata = new HashMap<>();
        private String id;
        private String serviceName;
        private String protocol;
        private String host;
        private int port;

        private Builder() {
        }

        /**
         * Sets the explicit instance id.
         *
         * @param id instance id
         * @return this builder
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the logical service name.
         *
         * @param serviceName service name
         * @return this builder
         */
        public Builder serviceName(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        /**
         * Sets the protocol and mirrors it into instance metadata.
         *
         * @param protocol protocol name
         * @return this builder
         */
        public Builder protocol(String protocol) {
            this.protocol = protocol;
            if (StringUtil.isNotBlank(protocol)) {
                metadata.put(KeyConstant.PROTOCOL, protocol);
            }
            return this;
        }

        /**
         * Sets the host or IP address.
         *
         * @param host host or IP address
         * @return this builder
         */
        public Builder host(String host) {
            this.host = host;
            return this;
        }

        /**
         * Sets the listening port.
         *
         * @param port listening port
         * @return this builder
         */
        public Builder port(int port) {
            this.port = port;
            return this;
        }

        /**
         * Adds metadata entries to the instance.
         *
         * @param metadata metadata entries
         * @return this builder
         */
        public Builder addMetadata(Map<String, String> metadata) {
            if (CollectionUtil.isNotEmpty(metadata)) {
                this.metadata.putAll(metadata);
            }
            return this;
        }

        /**
         * Adds one metadata entry to the instance.
         *
         * @param key   metadata key
         * @param value metadata value
         * @return this builder
         */
        public Builder addMetadata(String key, String value) {
            metadata.put(key, value);
            return this;
        }

        @Override
        public DefaultServiceInstance build() {
            return new DefaultServiceInstance(this);
        }
    }
}
