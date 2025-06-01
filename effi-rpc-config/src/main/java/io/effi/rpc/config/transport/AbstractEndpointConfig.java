package io.effi.rpc.config.transport;

import io.effi.rpc.config.AbstractNamedConfig;
import io.effi.rpc.config.Config;
import io.effi.rpc.util.AssertUtil;

/**
 * Provides an abstract implementation of {@link EndpointConfig}.
 */
public abstract class AbstractEndpointConfig extends AbstractNamedConfig implements EndpointConfig {

    protected CertificateConfig certificateConfig;

    protected TrafficShapingConfig trafficShapingConfig;

    protected ProtocolStack protocolStack;

    protected AbstractEndpointConfig(String protocol, String name, Config config, ProtocolStack protocolStack,
                                     CertificateConfig certificateConfig, TrafficShapingConfig trafficShapingConfig) {
        super(protocol, name, config);
        this.protocolStack = AssertUtil.notNull(protocolStack, "protocolStack");
        this.certificateConfig = certificateConfig;
        this.trafficShapingConfig = trafficShapingConfig;
    }

    @Override
    public ProtocolStack protocolStack() {
        return protocolStack;
    }

    @Override
    public CertificateConfig certificateConfig() {
        return certificateConfig;
    }

    @Override
    public TrafficShapingConfig trafficShapingConfig() {
        return trafficShapingConfig;
    }
}
