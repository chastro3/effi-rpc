package io.effi.rpc.transport;

import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.spi.Extensible;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;

import java.net.InetSocketAddress;

/**
 * Creates clients and servers.
 */
@Extensible
public interface Transporter {

    /**
     * Connects to a remote server.
     *
     * @param url    the client configuration
     * @param module the associated module
     * @return the connected client
     */
    Client connect(ClientConfig config, InetSocketAddress remoteAddress, EffiRpcModule module);

    /**
     * Binds a server to the specified URL.
     *
     * @param url    the server configuration
     * @param module the associated module
     * @return the bound server
     */
    Server bind(ServerConfig config, InetSocketAddress address, EffiRpcModule module);
}

