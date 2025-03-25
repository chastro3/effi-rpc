package io.effi.rpc.contract.config;

import io.effi.rpc.common.util.StringUtil;

/**
 * Configuration for client.
 *
 * @see NamedConfig
 * @see io.effi.rpc.support.builder.ClientConfigBuilder
 */
public interface ClientConfig extends NamedConfig {

    /**
     * The protocol of client.
     *
     * @return the protocol of client.
     */
    String protocol();

    @Override
    default String managerKey() {
        return StringUtil.isBlankOrDefault(name(), protocol());
    }
}


