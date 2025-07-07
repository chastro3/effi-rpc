package io.effi.rpc.config.registry;

import io.effi.rpc.component.DynamicTagComponent;
import io.effi.rpc.config.AbstractNamedConfig;
import io.effi.rpc.config.URL;

import java.util.Set;

/**
 * Provides the default implementation of {@link RegistryConfig}.
 */
public class DefaultRegistryConfig extends AbstractNamedConfig implements RegistryConfig {

    private final DynamicTagComponent dynamicTagComponent = new DynamicTagComponent();

    protected URL url;

    DefaultRegistryConfig(String protocol, String name, URL url) {
        super(protocol, name, url.params());
        this.url = url;
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
    public URL url() {
        return url;
    }


    @Override
    public String toString() {
        return url().toString();
    }

    /**
     * Builds {@link DefaultRegistryConfig} instance.
     */
    public static class Builder extends RegistryConfigBuilder<DefaultRegistryConfig, Builder> {

        @Override
        protected DefaultRegistryConfig build(URL url) {
            return new DefaultRegistryConfig(protocol, name, url);
        }
    }
}

