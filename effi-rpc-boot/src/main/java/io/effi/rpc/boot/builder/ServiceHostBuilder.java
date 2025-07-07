package io.effi.rpc.boot.builder;

import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.config.Config;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.FlatConfig;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.config.transport.ServerConfig;
import io.effi.rpc.constant.SystemKeys;
import io.effi.rpc.util.FluentBuilder;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;

import java.net.InetSocketAddress;

/**
 * Builds {@link ServiceHost} instance and defines configuration.
 */
public abstract class ServiceHostBuilder<T extends ServiceHost, C extends ServiceHostBuilder<T, C>>
        implements FluentBuilder<T, C>, Config.Provider {

    protected final Config config = new FlatConfig();

    protected InetSocketAddress exportedAddress;

    protected ServerConfig serverConfig;

    protected RegistryConfig[] registryConfigs;

    protected EffiRpcPlatform platform;

    /**
     * Sets the server configuration.
     */
    public C serverConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        return returnThis();
    }

    /**
     * Sets the platform.
     */
    public C platform(EffiRpcPlatform platform) {
        this.platform = platform;
        return returnThis();
    }

    /**
     * Sets the registry configurations.
     */
    public C registryAt(RegistryConfig... registryConfigs) {
        this.registryConfigs = registryConfigs;
        return returnThis();
    }

    /**
     * Sets the weight(used for load balancing).
     */
    public C weight(int weight) {
        config.set(DefaultConfigNames.WEIGHT, weight);
        return returnThis();
    }

    /**
     * Sets the exported address from a string representation (e.g., "127.0.0.1:8080").
     */
    public C exportedAddress(String exportedAddress) {
        return exportedAddress(NetUtil.toInetSocketAddress(exportedAddress));
    }

    /**
     * Sets the exported address using an IP and port.
     */
    public C exportedAddress(String ip, int port) {
        if (NetUtil.isValidIP(ip) && NetUtil.isValidPort(port)) {
            return exportedAddress(NetUtil.toAddress(ip, port));
        }
        return returnThis();
    }

    /**
     * Sets the exported address using an {@link InetSocketAddress}.
     */
    public C exportedAddress(InetSocketAddress address) {
        this.exportedAddress = address;
        return returnThis();
    }

    /**
     * Sets the exported address using a port number and automatically determines the IP address.
     */
    public C exportedPort(int port) {
        if (NetUtil.isValidPort(port)) {
            String configIp = System.getProperty(SystemKeys.LOCAL_IP);
            String ip = StringUtil.isBlankOrDefault(configIp, NetUtil.defaultHost());
            return exportedAddress(ip, port);
        }
        throw new IllegalArgumentException("Invalid port:" + port);
    }

    @Override
    public Config config() {
        return config;
    }
}

