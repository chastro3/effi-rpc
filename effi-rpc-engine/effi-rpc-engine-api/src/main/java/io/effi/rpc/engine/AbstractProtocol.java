package io.effi.rpc.engine;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.url.*;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.parameter.Argument;
import io.effi.rpc.contract.parameter.Body;
import io.effi.rpc.contract.parameter.ParamVar;
import io.effi.rpc.contract.parameter.PathVar;
import io.effi.rpc.protocol.InitializedConfig;
import io.effi.rpc.protocol.Protocol;
import io.effi.rpc.protocol.client.Client;
import io.effi.rpc.protocol.codec.ClientCodec;
import io.effi.rpc.protocol.codec.ServerCodec;
import io.effi.rpc.protocol.server.Server;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of {@link Protocol}.
 */
public abstract class AbstractProtocol implements Protocol {

    protected static final String SERVER_INVOKE_EXCEPTION = "Server invoke exception: ";

    protected final Map<String, Client> clients = new ConcurrentHashMap<>();

    protected final Map<String, Server> servers = new ConcurrentHashMap<>();

    protected String protocol;

    protected ServerCodec serverCodec;

    protected ClientCodec clientCodec;

    protected AbstractProtocol(String protocol) {
        this(protocol, null, null);
    }

    protected AbstractProtocol(String protocol, ServerCodec serverCodec, ClientCodec clientCodec) {
        this.protocol = protocol;
        this.serverCodec = serverCodec;
        this.clientCodec = clientCodec;
    }

    @Override
    public Client openClient(URL url, EffiRpcModule module) {
        String key = url.getParam(KeyConstant.NAME, url.protocol());
        Client client = clients.computeIfAbsent(key, k -> connect(initClientConfig(url, module)));
        if (!client.isActive()) {
            client.connect();
        }
        return client;
    }

    @Override
    public Server openServer(URL url, EffiRpcModule module) {
        String key = url.getParam(KeyConstant.NAME, url.authority());
        return servers.computeIfAbsent(key, k -> bind(initServerConfig(url, module)));
    }

    @Override
    public Envelope.Request createRequest(Caller<?> caller, Object[] args) {
        Wrapper wrapper = parseWrapper(caller, args);
        return createRequest(caller, wrapper.url(), wrapper.body());
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

    protected abstract Client connect(InitializedConfig config);

    protected abstract Server bind(InitializedConfig config);

    protected abstract InitializedConfig initClientConfig(URL url, EffiRpcModule module);

    protected abstract InitializedConfig initServerConfig(URL url, EffiRpcModule module);

    protected abstract Envelope.Request createRequest(Caller<?> caller, URL requestUrl, Object body);

    private Wrapper parseWrapper(Caller<?> caller, Object[] args) {
        Map<String, String> pathVars = new HashMap<>();
        Map<String, String> paramVars = new HashMap<>();
        Object body = null;
        if (CollectionUtil.isNotEmpty(args)) {
            for (Object arg : args) {
                if (arg instanceof PathVar<?> pathVar && pathVar.get() instanceof Argument.Target target) {
                    pathVars.putAll(target.get());
                } else if (arg instanceof ParamVar<?> paramVar && paramVar.get() instanceof Argument.Target target) {
                    paramVars.putAll(target.get());
                } else if (arg instanceof Body<?> bodyVar) {
                    body = bodyVar.get();
                }
            }
        }
        QueryPath queryPath = caller.queryPath();
        URLBuilder urlBuilder = URL.builder()
                .type(URLType.REQUEST)
                .protocol(protocol())
                .address(Constant.UNKNOWN_ADDRESS);
        if (queryPath != null) {
            List<String> paths = queryPath.paths();
            if (CollectionUtil.isNotEmpty(paths)) {
                for (int i = 0; i < paths.size(); i++) {
                    String path = paths.get(i);
                    String var = URLUtil.getVar(path);
                    if (!StringUtil.isBlank(var)) {
                        paths.add(i, pathVars.getOrDefault(var, path));
                    }
                }
                urlBuilder.paths(paths);
            }
            Map<String, String> queryParams = queryPath.queryParams();
            if (CollectionUtil.isNotEmpty(queryParams)) {
                urlBuilder.params(queryParams);
            }
            if (CollectionUtil.isNotEmpty(paramVars)) {
                urlBuilder.params(paramVars);
            }
        }
        return new Wrapper(urlBuilder.build(), body);
    }

    private record Wrapper(URL url, Object body) {}

}
