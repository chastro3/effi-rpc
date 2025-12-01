package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.config.ConfigurableOptionName;
import io.effi.rpc.config.IdentifiableConfig;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.config.Options;

/**
 * Implements {@link ClientConfig} using http2.
 */
public class Http2ClientConfig extends Http2EndpointConfig implements ClientConfig {

    public static final OptionName<Boolean> PUSH_ENABLED = ConfigurableOptionName.<Boolean>nameOf("pushEnabled").defaultValue(false);

    private static final Http2ClientConfig DEFAULT_CONFIG = builder().id("default-http2").build();

    Http2ClientConfig(String id, Options options) {
        super(id, options);
    }

    public static Http2ClientConfig defaultConfig() {
        return DEFAULT_CONFIG;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builds {@link Http2ClientConfig} instance.
     */
    public static class Builder extends IdentifiableConfig.Builder<Http2ClientConfig, Builder>
            implements Http2EndpointConfig.Configurator<Http2ClientConfig.Builder> {

        public Builder enablePush(boolean enablePush) {
            addOption(PUSH_ENABLED, enablePush);
            return this;
        }

        @Override
        public Http2ClientConfig build() {
            return new Http2ClientConfig(id, options);
        }
    }
}
