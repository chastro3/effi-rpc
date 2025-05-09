package io.effi.rpc.engine.builder;

import io.effi.rpc.config.Config;
import io.effi.rpc.config.ConfigSource;
import io.effi.rpc.config.FlatConfig;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.SystemKey;
import io.effi.rpc.util.FluentBuilder;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.ServerExporter;

import java.net.InetSocketAddress;

/**
 * Builds {@link ServerExporter} instances.
 */
public abstract class ServerExportBuilder<T extends ServerExporter, C extends ServerExportBuilder<T, C>>
        implements FluentBuilder<T, C>, ConfigSource {

    protected int weight = Constant.DEFAULT_WEIGHT;

    protected InetSocketAddress exportedAddress;

    protected ServerConfig serverConfig;

    /**
     * The module associated with the exporter.
     */
    protected EffiRpcModule module;

    /**
     * Sets the server configuration.
     */
    public C serverConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        return returnThis();
    }

    /**
     * Sets the module.
     */
    public C module(EffiRpcModule module) {
        this.module = module;
        return returnThis();
    }

    /**
     * Sets the weight(used for load balancing).
     */
    public C weight(int weight) {
        this.weight = weight;
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
            String configIp = System.getProperty(SystemKey.LOCAL_IP);
            String ip = StringUtil.isBlankOrDefault(configIp, NetUtil.defaultHost());
            return exportedAddress(ip, port);
        }
        throw new IllegalArgumentException("Invalid port:" + port);
    }

    @Override
    public Config config() {
        Config config = new FlatConfig();
        config.set(KeyConstant.WEIGHT, String.valueOf(weight));
        return config;
    }
}

