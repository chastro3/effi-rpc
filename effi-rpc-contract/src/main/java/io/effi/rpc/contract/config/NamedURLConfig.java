package io.effi.rpc.contract.config;

import io.effi.rpc.common.config.Config;
import io.effi.rpc.common.config.URLSource;

/**
 * Configuration that can be converted to a {@link io.effi.rpc.common.config.URL}
 * and uses URL parameters for dynamic settings.
 */
public interface NamedURLConfig extends NamedConfig, URLSource {

    @Override
    default Config config() {
        return url().params();
    }
}
