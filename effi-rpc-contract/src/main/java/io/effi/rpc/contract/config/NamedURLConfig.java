package io.effi.rpc.contract.config;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.URLSource;

/**
 * Defines configuration that can be converted to a {@link URL} and uses URL parameters for dynamic settings.
 */
public interface NamedURLConfig extends NamedConfig, URLSource {

    @Override
    default Config config() {
        return url().params();
    }
}
