package io.effi.rpc.contract.config;

import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.URLSource;

/**
 * Configuration that can be converted to a {@link io.effi.rpc.common.url.URL}
 * and uses URL parameters for dynamic settings.
 */
public interface NamedURLConfig extends NamedConfig, URLSource {

    @Override
    default Config config() {
        return url().params();
    }
}
