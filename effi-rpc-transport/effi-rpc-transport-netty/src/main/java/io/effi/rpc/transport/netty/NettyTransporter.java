package io.effi.rpc.transport.netty;

import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.Transporter;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;

import java.net.InetSocketAddress;

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
    Client connect(NettyEndpointConfig config);

    /**
     * Binds a server with the given configuration.
     *
     * @param config the initialized server configuration
     * @return the bound server
     */
    Server bind(NettyEndpointConfig config);

    /**
     * Initializes the client configuration based on the given URL and module.
     *
     * @param url     the server URL
     * @param module  the associated module
     * @return the initialized client configuration
     */
    NettyEndpointConfig initClientConfig(ClientConfig config, InetSocketAddress remoteAddress, EffiRpcModule module);

    /**
     * Initializes the server configuration based on the given URL and module.
     *
     * @param url     the server URL
     * @param module  the associated module
     * @return the initialized server configuration
     */
    NettyEndpointConfig initServerConfig(ServerConfig config, InetSocketAddress address, EffiRpcModule module);

    @Override
    default Client connect(ClientConfig config, InetSocketAddress remoteAddress, EffiRpcModule module) {
        return connect(initClientConfig(config, remoteAddress, module));
    }

    @Override
    default Server bind(ServerConfig config, InetSocketAddress address, EffiRpcModule module) {
        return bind(initServerConfig(config, address, module));
    }
}

