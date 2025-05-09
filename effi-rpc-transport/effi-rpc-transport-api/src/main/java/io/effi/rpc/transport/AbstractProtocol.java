package io.effi.rpc.transport;

import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.contract.config.EndpointConfig;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.spi.ExtensionLoader;
import io.effi.rpc.transport.codec.ClientCodec;
import io.effi.rpc.transport.codec.ServerCodec;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an abstract implementation of {@link Protocol}.
 */
public abstract class AbstractProtocol implements Protocol {

    protected static final Map<String, Transporter> TRANSPORTERS = new ConcurrentHashMap<>();

    protected static final String SERVER_INVOKE_EXCEPTION = "Server invoke exception: ";

    protected final Map<String, Client> clients = new ConcurrentHashMap<>();

    protected final Map<String, Server> servers = new ConcurrentHashMap<>();

    protected String protocol;

    protected Transporter transporter;

    protected ServerCodec serverCodec;

    protected ClientCodec clientCodec;

    protected AbstractProtocol(String protocol, Transporter transporter) {
        this(protocol, transporter, null, null);
    }

    protected AbstractProtocol(String protocol, Transporter transporter, ServerCodec serverCodec, ClientCodec clientCodec) {
        this.protocol = AssertUtil.notBlank(protocol, "protocol");
        this.transporter = transporter;
        this.serverCodec = AssertUtil.notNull(serverCodec, "serverCodec");
        this.clientCodec = AssertUtil.notNull(clientCodec, "clientCodec");
    }

    @Override
    public Client openClient(ClientConfig config, InetSocketAddress remoteAddress, EffiRpcModule module) {
        Client client = clients.computeIfAbsent(NetUtil.toAddress(remoteAddress),
                k -> getTransporter(config).connect(config, remoteAddress, module));
        if (!client.isActive()) {
            client.connect();
        }
        return client;
    }

    @Override
    public Server openServer(ServerConfig config, InetSocketAddress address, EffiRpcModule module) {
        return servers.computeIfAbsent(NetUtil.toAddress(address), k -> getTransporter(config).bind(config, address, module));
    }

    @Override
    public ServerCodec serverCodec() {
        return serverCodec;
    }

    @Override
    public ClientCodec clientCodec() {
        return clientCodec;
    }

    @Override
    public String protocol() {
        return protocol;
    }

    @Override
    public Collection<Client> clients() {
        return clients.values();
    }

    @Override
    public Collection<Server> servers() {
        return servers.values();
    }

    @Override
    public synchronized void clear() {
        clients().forEach(Client::close);
        servers().forEach(Server::close);
        clients.clear();
        servers.clear();
    }

    public Transporter getTransporter(EndpointConfig config) {
        String transporterName = config.getParam(KeyConstant.TRANSPORTER);
        if (StringUtil.isBlank(transporterName) && transporter != null) {
            return transporter;
        }
        return TRANSPORTERS.computeIfAbsent(transporterName,
                key -> ExtensionLoader.loadExtension(Transporter.class, key));
    }

}
