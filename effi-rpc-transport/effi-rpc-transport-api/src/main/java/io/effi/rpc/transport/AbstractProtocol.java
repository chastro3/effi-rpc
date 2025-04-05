package io.effi.rpc.transport;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.spi.ExtensionLoader;
import io.effi.rpc.common.config.URL;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.codec.ClientCodec;
import io.effi.rpc.transport.codec.ServerCodec;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of {@link Protocol}.
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
    public Client openClient(URL url, EffiRpcModule module) {
        String key = url.getParam(KeyConstant.NAME, url.protocol());
        Client client = clients.computeIfAbsent(key, k -> getTransporter(url).connect(url, module));
        if (!client.isActive()) {
            client.connect();
        }
        return client;
    }

    @Override
    public Server openServer(URL url, EffiRpcModule module) {
        String key = url.getParam(KeyConstant.NAME, url.authority());
        return servers.computeIfAbsent(key, k -> getTransporter(url).bind(url, module));
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

    protected Transporter getTransporter(URL url) {
        String transporterName = url.getParam(KeyConstant.TRANSPORTER);
        if (StringUtil.isBlank(transporterName) && transporter != null) {
            return transporter;
        }
        return TRANSPORTERS.computeIfAbsent(transporterName,
                key -> ExtensionLoader.loadExtension(Transporter.class, key));
    }

}
