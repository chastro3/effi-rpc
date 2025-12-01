package io.effi.rpc.component.transport.support;

import io.effi.rpc.component.transport.CertificateConfig;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.component.transport.ProtocolStack;
import io.effi.rpc.config.IdentifiableConfig;
import io.effi.rpc.config.Options;
import io.effi.rpc.util.AssertUtil;

/**
 * Provides an abstract implementation of {@link EndpointConfig}.
 */
public abstract class AbstractEndpointConfig extends IdentifiableConfig implements EndpointConfig {

    protected String protocol;

    protected CertificateConfig certificateConfig;

    protected ProtocolStack protocolStack;

    protected AbstractEndpointConfig(String protocol, ProtocolStack protocolStack, String id, Options options) {
        super(checkId(id, protocol), options);
        this.protocol = AssertUtil.notBlank(protocol, "protocol");
        this.protocolStack = AssertUtil.notNull(protocolStack, "protocolStack");
        this.certificateConfig = options.option(CertificateConfig.NAME);
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
