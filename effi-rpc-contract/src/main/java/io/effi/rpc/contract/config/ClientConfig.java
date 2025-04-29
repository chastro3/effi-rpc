package io.effi.rpc.contract.config;

import io.effi.rpc.util.StringUtil;

/**
 * Configuration for client.
 */
public interface ClientConfig extends NamedConfig {

    /**
     * The protocol of client.
     */
    String protocol();

    @Override
    default String repositoryKey() {
        return StringUtil.isBlankOrDefault(name(), protocol());
    }
}


