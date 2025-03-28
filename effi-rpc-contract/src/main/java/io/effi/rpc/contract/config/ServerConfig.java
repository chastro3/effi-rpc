package io.effi.rpc.contract.config;

import io.effi.rpc.common.util.StringUtil;

/**
 * Configuration for server.
 *
 * @see NamedConfig
 * @see io.effi.rpc.engine.builder.ServerConfigBuilder
 */
public interface ServerConfig extends NamedConfig {

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

