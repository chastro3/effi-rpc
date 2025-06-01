package io.effi.rpc.config.transport;

import io.effi.rpc.component.PlatformSource;
import io.effi.rpc.config.NamedConfig;
import io.effi.rpc.config.URL;
import io.effi.rpc.util.StringUtil;

import java.net.InetSocketAddress;

/**
 * Defines configuration for endpoint.
 */
public interface EndpointConfig extends NamedConfig, PlatformSource {

    /**
     * Returns the protocol of the endpoint.
     */
    String protocol();

    /**
     * Returns the protocol stack of the endpoint.
     */
    ProtocolStack protocolStack();

    /**
     * Returns the certificate configuration of the endpoint.
     */
    CertificateConfig certificateConfig();

    /**
     * Returns the traffic shaping configuration of the endpoint.
     */
    TrafficShapingConfig trafficShapingConfig();

    /**
     * Creates a URL based on the provided network address.
     *
     * @param address the network address
     * @return the URL
     */
    URL newUrl(InetSocketAddress address);

    @Override
    default String id() {
        return StringUtil.isBlankOrDefault(name(), protocol());
    }

}

