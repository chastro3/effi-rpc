package io.effi.rpc.component.registry;

import io.effi.rpc.component.DynamicTagComponent;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.IdentifiableConfig;
import io.effi.rpc.util.AssertUtil;

import java.util.Set;

/**
 * Provides the default implementation of {@link RegistryConfig}.
 */
public class DefaultRegistryConfig extends IdentifiableConfig implements RegistryConfig {

    private final String type;

    private final String address;

    private final ThreadPool threadPool;

    private final DynamicTagComponent dynamicTagComponent = new DynamicTagComponent();

    DefaultRegistryConfig(String id, Config config, String type, String address, ThreadPool threadPool) {
        super(checkId(id, type), config);
        this.type = AssertUtil.notBlank(type, "protocol");
        this.address = AssertUtil.notBlank(address, "address");
        this.threadPool = threadPool;
    }

    public static Builder builder() {
        return new Builder();
    }

    public DefaultRegistryConfig addTags(String... tags) {
        dynamicTagComponent.addTags(tags);
        return this;
    }

    @Override
    public Set<String> tags() {
        return dynamicTagComponent.tags();
    }

    @Override
    public String type() {
        return type;
    }

    @Override
    public String address() {
        return address;
    }

    @Override
    public ThreadPool threadPool() {
        return threadPool;
    }

    @Override
    public String toString() {
        return type + "://" + address;
    }

    /**
     * Builds {@link DefaultRegistryConfig} instance.
     */
    public static class Builder extends RegistryConfigBuilder<DefaultRegistryConfig, Builder> {

        @Override
        public DefaultRegistryConfig build() {
            return new DefaultRegistryConfig(id, config, type, address, threadPool);
        }
    }
}

