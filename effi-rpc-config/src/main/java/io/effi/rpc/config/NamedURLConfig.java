package io.effi.rpc.config;

/**
 * Defines configuration that can be converted to a {@link URL} and uses URL parameters for dynamic settings.
 */
public interface NamedURLConfig extends NamedConfig, URL.Provider {

    @Override
    default Config config() {
        return url().params();
    }
}
