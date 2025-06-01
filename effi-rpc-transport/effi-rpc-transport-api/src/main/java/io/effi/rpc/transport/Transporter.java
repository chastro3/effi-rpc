package io.effi.rpc.transport;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.resoruce.Cleanable;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Controls and manages endpoint.
 */
public interface Transporter extends Cleanable {

    /**
     * Returns an existing or newly created server.
     *
     * @param config  the server config
     * @param address the bind address
     * @param platform  the module
     * @return server instance
     */
    Server getServer(ServerConfig config, InetSocketAddress address, EffiRpcPlatform platform);

    /**
     * Returns an existing or newly created client.
     *
     * @param config        the client config
     * @param remoteAddress the remote  address
     * @param platform        the module
     * @return client instance
     */
    Client getClient(ClientConfig config, InetSocketAddress remoteAddress, EffiRpcPlatform platform);

    /**
     * Returns all managed servers.
     */
    Collection<Server> servers();

    /**
     * Returns all managed clients.
     */
    Collection<Client> clients();
}


