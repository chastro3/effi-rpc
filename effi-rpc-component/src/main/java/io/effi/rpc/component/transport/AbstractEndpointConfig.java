package io.effi.rpc.component.transport;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.IdentifiableConfig;
import io.effi.rpc.util.AssertUtil;

/**
 * Provides an abstract implementation of {@link EndpointConfig}.
 */
public abstract class AbstractEndpointConfig extends IdentifiableConfig implements EndpointConfig {

    protected String protocol;

    protected CertificateConfig certificateConfig;

    protected ProtocolStack protocolStack;

    protected AbstractEndpointConfig(String id, Config config, String protocol,
                                     ProtocolStack protocolStack, CertificateConfig certificateConfig) {
        super(checkId(id, protocol), config);
        this.protocol = AssertUtil.notBlank(protocol, "protocol");
        this.protocolStack = AssertUtil.notNull(protocolStack, "protocolStack");
        this.certificateConfig = certificateConfig;
    }

    @Override
    public String protocolName() {
        return protocol;
    }

    @Override
    public ProtocolStack protocolStack() {
        return protocolStack;
    }

    @Override
    public CertificateConfig certificateConfig() {
        return certificateConfig;
    }
}
