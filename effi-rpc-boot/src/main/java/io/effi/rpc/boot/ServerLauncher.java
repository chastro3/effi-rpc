package io.effi.rpc.boot;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.transport.ServerConfig;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.registry.util.RegistryUtil;
import io.effi.rpc.transport.TransportProtocol;
import io.effi.rpc.transport.endpoint.Server;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.trait.Closeable;
import io.effi.rpc.trait.Identifiable;
import io.effi.rpc.util.LazySingleton;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.APPLICATION;

/**
 * Manages the lifecycle of one server endpoint.
 */
@ScopedComponent(scope = APPLICATION)
public class ServerLauncher extends ScopedApplication.Holder implements Closeable, Identifiable {

    private static final Logger logger = LoggerFactory.getLogger(ServerLauncher.class);
    private static final Map<String, ServerLauncher> SERVER_LAUNCHERS = new ConcurrentHashMap<>();

    private final String id;
    private final ServerConfig serverConfig;
    private final InetSocketAddress boundAddress;
    private final int weight;
    private final LazySingleton<Server> server = LazySingleton.from(this::startServer);

    private ServerLauncher(
            String id,
            ScopedApplication application,
            ServerConfig serverConfig,
            InetSocketAddress boundAddress,
            int weight
    ) {
        super(application);
        this.id = id;
        this.serverConfig = serverConfig;
        this.boundAddress = boundAddress;
        this.weight = weight;
        application.registry().register(ServerLauncher.class, this);
    }

    /**
     * Attaches a server bound to the local host.
     *
     * @param application owning application
     * @param config server configuration
     * @param port bound port
     * @return attached server launcher
     */
    public static ServerLauncher attach(ScopedApplication application, ServerConfig config, int port) {
        return attach(application, config, NetUtil.localHost(), port);
    }

    /**
     * Attaches a server bound to the supplied host.
     *
     * @param application owning application
     * @param config server configuration
     * @param host bound host
     * @param port bound port
     * @return attached server launcher
     */
    public static ServerLauncher attach(ScopedApplication application, ServerConfig config, String host, int port) {
        return attach(application, config, InetSocketAddress.createUnresolved(host, port));
    }

    /**
     * Attaches a server bound to the supplied address.
     *
     * @param application owning application
     * @param config server configuration
     * @param boundAddress bound address
     * @return attached server launcher
     */
    public static ServerLauncher attach(ScopedApplication application, ServerConfig config, InetSocketAddress boundAddress) {
        return attach(application, config, boundAddress, 1);
    }

    /**
     * Attaches a weighted server bound to the supplied address.
     *
     * @param application owning application
     * @param config server configuration
     * @param boundAddress bound address
     * @param weight server weight
     * @return attached server launcher
     */
    public static ServerLauncher attach(ScopedApplication application, ServerConfig config, InetSocketAddress boundAddress, int weight) {
        AssertUtil.notNull(application, "application");
        AssertUtil.notNull(config, "server config");
        AssertUtil.notNull(boundAddress, "bound address");
        String id = RegistryUtil.generateId(config.protocolName(), boundAddress);
        return SERVER_LAUNCHERS.compute(id, (k, existing) -> {
            if (existing == null) return new ServerLauncher(id, application, config, boundAddress, weight);
            if (existing.application() == application) return existing;
            throw new IllegalStateException(StringUtil.format(
                    "Server [{}] with id '{}' already allocated to application '{}'",
                    config.protocolName(), id, existing.application().name()
            ));
        });
    }

    /**
     * Starts the server and returns its endpoint.
     */
    public Server start() {
        return server.ensure();
    }

    /**
     * Returns the active server endpoint when available.
     */
    public Optional<Server> server() {
        return active()
                ? Optional.of(server.ensure())
                : Optional.empty();
    }

    /**
     * Returns the server configuration.
     */
    public ServerConfig serverConfig() {
        return serverConfig;
    }

    /**
     * Returns the bound address.
     */
    public InetSocketAddress boundAddress() {
        return boundAddress;
    }

    /**
     * Returns the server weight.
     */
    public int weight() {
        return weight;
    }

    @Override
    public void close() {
        if (server.initialized()) {
            server.ensure().close();
        }
        SERVER_LAUNCHERS.remove(id, this);
    }

    @Override
    public boolean active() {
        return server.initialized()
                && server.ensure().active();
    }

    @Override
    public String id() {
        return id;
    }

    private Server startServer() {
        ScopedPlatform platform = platform();
        TransportProtocol protocol = platform.namedExtension(TransportProtocol.class, serverConfig.protocolName());
        Server server = protocol.supplyServer(serverConfig, boundAddress, platform);
        server.bind().onComplete(res -> {
            if (res.succeeded()) {
                logger.info("({}) Server started on port {}.", serverConfig.protocolName().toUpperCase(), boundAddress.getPort());
            } else {
                logger.error("Failed to open ({}) server on port {}.", res.cause(), serverConfig.protocolName(), boundAddress.getPort());
            }
        });
        return server;
    }
}

