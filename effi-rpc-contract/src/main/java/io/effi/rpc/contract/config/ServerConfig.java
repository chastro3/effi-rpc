package io.effi.rpc.contract.config;

import io.effi.rpc.util.StringUtil;

/**
 * Configuration for server.
 *
 * @see NamedConfig
 * @see io.effi.rpc.engine.builder.ServerConfigBuilder
 */
public interface ServerConfig extends NamedConfig {

    /**
     * The protocol of server.
     */
    String protocol();

    @Override
    default String repositoryKey() {
        return StringUtil.isBlankOrDefault(name(), protocol());
    }

}

