package io.effi.rpc.contract.config;

import io.effi.rpc.config.URL;
import io.effi.rpc.util.StringUtil;

import java.net.InetSocketAddress;

/**
 * Defines configuration for endpoint.
 */
public interface EndpointConfig extends NamedConfig {

    /**
     * Returns the protocol of the endpoint.
     */
    String protocol();

    /**
     * Returns the certificate configuration of the endpoint.
     */
    CertificateConfig certificate();

    /**
     * Creates a URL based on the provided network address.
     *
     * @param address the network address
     * @return the URL
     */
    URL newUrl(InetSocketAddress address);

    @Override
    default String repositoryKey() {
        return StringUtil.isBlankOrDefault(name(), protocol());
    }

}

