package io.effi.rpc.transport.netty;

import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.Transporter;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;

/**
 * Netty implementation of {@link Transporter}.
 */
public interface NettyTransporter extends Transporter {

    /**
     * Connects to a remote server with the given configuration.
     *
     * @param config the initialized client configuration
     * @return the connected client
     */
    Client connect(InitializedConfig config);

    /**
     * Binds a server with the given configuration.
     *
     * @param config the initialized server configuration
     * @return the bound server
     */
    Server bind(InitializedConfig config);

    /**
     * Initializes the client configuration based on the given URL and module.
     *
     * @param url     the server URL
     * @param module  the associated module
     * @return the initialized client configuration
     */
    InitializedConfig initClientConfig(URL url, EffiRpcModule module);

    /**
     * Initializes the server configuration based on the given URL and module.
     *
     * @param url     the server URL
     * @param module  the associated module
     * @return the initialized server configuration
     */
    InitializedConfig initServerConfig(URL url, EffiRpcModule module);

    @Override
    default Client connect(URL url, EffiRpcModule module) {
        return connect(initClientConfig(url, module));
    }

    @Override
    default Server bind(URL url, EffiRpcModule module) {
        return bind(initServerConfig(url, module));
    }
}

