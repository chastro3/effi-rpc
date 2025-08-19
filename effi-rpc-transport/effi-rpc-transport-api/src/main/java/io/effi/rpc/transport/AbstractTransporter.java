package io.effi.rpc.transport;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ClientConfig;
import io.effi.rpc.component.transport.ServerConfig;
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
    public Server supplyServer(ServerConfig config, InetSocketAddress address, ScopedPlatform platform) {
        return servers.computeIfAbsent(NetUtil.toAddress(address), k -> createServer(config, address, platform));
    }

    @Override
    public Client supplyClient(ClientConfig config, InetSocketAddress remoteAddress, ScopedPlatform platform) {
        return clients.computeIfAbsent(NetUtil.toAddress(remoteAddress), k -> createClient(config, remoteAddress, platform));
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

    protected abstract Server createServer(ServerConfig config, InetSocketAddress address, ScopedPlatform platform);

    protected abstract Client createClient(ClientConfig config, InetSocketAddress remoteAddress, ScopedPlatform platform);
}
