package io.effi.rpc.engine;

import io.effi.rpc.config.Config;
import io.effi.rpc.contract.config.CertificateConfig;
import io.effi.rpc.contract.config.EndpointConfig;

/**
 * Abstract implementation of {@link EndpointConfig}.
 */
public abstract class AbstractEndpointConfig extends AbstractNamedConfig implements EndpointConfig {

    protected CertificateConfig certificateConfig;

    protected AbstractEndpointConfig(String protocol, String name, Config config, CertificateConfig certificateConfig) {
        super(protocol, name, config);
        this.certificateConfig = certificateConfig;
    }

    @Override
    public CertificateConfig certificate() {
        return certificateConfig;
    }
}
