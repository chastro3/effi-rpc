package io.effi.rpc.transport;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.NetUtil;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an abstract implementation of {@link Transporter}.
 */
public abstract class AbstractTransporter implements Transporter {

    protected final Map<String, Client> clients = new ConcurrentHashMap<>();

    protected final Map<String, Server> servers = new ConcurrentHashMap<>();

    @Override
    public Server getServer(ServerConfig config, InetSocketAddress address, EffiRpcPlatform platform) {
        return servers.computeIfAbsent(NetUtil.toAddress(address), k -> newServer(config, address, platform));
    }

    @Override
    public Client getClient(ClientConfig config, InetSocketAddress remoteAddress, EffiRpcPlatform platform) {
        return clients.computeIfAbsent(NetUtil.toAddress(remoteAddress), k -> newClient(config, remoteAddress, platform));
    }

    @Override
    public Collection<Server> servers() {
        return Collections.unmodifiableCollection(servers.values());
    }

    @Override
    public Collection<Client> clients() {
        return Collections.unmodifiableCollection(clients.values());
    }

    @Override
    public void clear() {
        clients().forEach(Client::close);
        servers().forEach(Server::close);
        clients.clear();
        servers.clear();
    }

    protected abstract Server newServer(ServerConfig config, InetSocketAddress address, EffiRpcPlatform platform);

    protected abstract Client newClient(ClientConfig config, InetSocketAddress remoteAddress, EffiRpcPlatform platform);
}
