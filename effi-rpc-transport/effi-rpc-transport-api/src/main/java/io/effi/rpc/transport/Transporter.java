package io.effi.rpc.transport;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.trait.Cleanable;

import java.net.InetSocketAddress;
import java.util.Collection;

/**
 * Manages transport endpoints including clients and servers.
 * <p>
 * Provides transporter functionality for creating and managing
 * network endpoints with server and client support.
 */
public interface Transporter extends Cleanable {

    /**
     * Supplies a server for the given configuration and address.
     *
     * @param config   the server configuration
     * @param address  the bind address
     * @param platform the module
     * @return the supplied server
     */
    Server supplyServer(ServerConfig config, InetSocketAddress address, ScopedPlatform platform);

    /**
     * Supplies a client for the given configuration and remote address.
     *
     * @param config        the client configuration
     * @param remoteAddress the remote server address
     * @param platform      the module
     * @return the supplied client
     */
    Client supplyClient(ClientConfig config, InetSocketAddress remoteAddress, ScopedPlatform platform);

    /**
     * Returns all managed servers.
     */
    Collection<Server> servers();

    /**
     * Returns all managed clients.
     */
    Collection<Client> clients();
}


