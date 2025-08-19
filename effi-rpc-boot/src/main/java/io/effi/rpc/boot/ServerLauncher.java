package io.effi.rpc.boot;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.registry.util.RegistryUtil;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.Identifiable;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.resoruce.Closeable;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;

@ScopedComponent(scope = APPLICATION)
public class ServerLauncher extends ScopedApplication.Holder implements Closeable, Identifiable {

    private static final Logger logger = LoggerFactory.getLogger(ServerLauncher.class);
    private static final Map<String, ServerLauncher> SERVER_LAUNCHERS = new ConcurrentHashMap<>();

    private final String id;
    private final ServerConfig serverConfig;
    private final InetSocketAddress boundAddress;
    private final LazySingleton<Server> server = LazySingleton.from(this::startServer);

    private ServerLauncher(String id, ScopedApplication application, ServerConfig serverConfig, InetSocketAddress boundAddress) {
        super(application);
        this.id = id;
        this.serverConfig = serverConfig;
        this.boundAddress = boundAddress;
        application.registry().register(ServerLauncher.class, this);
    }

    public static ServerLauncher allocate(ScopedApplication application, ServerConfig config, int port) {
        return allocate(application, config, NetUtil.localHost(), port);
    }

    public static ServerLauncher allocate(ScopedApplication application, ServerConfig config, String host, int port) {
        return allocate(application, config, InetSocketAddress.createUnresolved(host, port));
    }

    public static ServerLauncher allocate(ScopedApplication application, ServerConfig config, InetSocketAddress boundAddress) {
        AssertUtil.notNull(application, "application");
        AssertUtil.notNull(config, "server config");
        AssertUtil.notNull(boundAddress, "bound address");
        String id = RegistryUtil.generateId(config.protocolName(), boundAddress);
        return SERVER_LAUNCHERS.compute(id, (k, existing) -> {
            if (existing == null) return new ServerLauncher(id, application, config, boundAddress);
            if (existing.application() == application) return existing;
            throw new IllegalStateException(StringUtil.format(
                    "Server [{}] with id '{}' already allocated to application '{}'",
                    config.protocolName(), id, existing.application().name()
            ));
        });
    }

    public Server start() {
        return server.ensure();
    }

    private Server startServer() {
        ScopedPlatform platform = platform();
        TransportProtocol protocol = platform.namedExtension(TransportProtocol.class, serverConfig.protocolName());
        Server server = protocol.supplyServer(serverConfig, boundAddress, platform);
        server.bind().onComplete(res -> {
            if (res.succeeded()) {
                logger.info("Opened ({}) server on port {}.", serverConfig.protocolName(), boundAddress.getPort());
            } else {
                logger.error("Failed to open ({}) server on port {}.", serverConfig.protocolName(), boundAddress.getPort(), res.cause());
            }
        });
        return server;
    }

    @Override
    public void close() {
        if (server.initialized()) {
            server.ensure().close();
        }
        SERVER_LAUNCHERS.remove(id, this);
    }

    @Override
    public boolean isActive() {
        return server.initialized()
                && server.ensure().isActive();
    }

    @Override
    public String id() {
        return id;
    }

    public Optional<Server> server() {
        return isActive()
                ? Optional.of(server.ensure())
                : Optional.empty();
    }

    public ServerConfig serverConfig() {
        return serverConfig;
    }

    public InetSocketAddress boundAddress() {
        return boundAddress;
    }
}

