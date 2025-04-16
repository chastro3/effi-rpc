package io.effi.rpc.engine.builder;

import io.effi.rpc.common.config.Config;
import io.effi.rpc.common.config.ConfigSource;
import io.effi.rpc.common.config.FlatConfig;
import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.constant.SystemKey;
import io.effi.rpc.common.util.ChainBuilder;
import io.effi.rpc.common.util.NetUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.config.ServerConfig;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.contract.ServerExporter;

import java.net.InetSocketAddress;

/**
 * Builder for configuring {@link ServerExporter} instances.
 *
 * @param <T> The type of {@link ServerExporter}.
 * @param <C> The type of the builder.
 */
public abstract class ServerExportBuilder<T extends ServerExporter, C extends ServerExportBuilder<T, C>>
        implements ChainBuilder<T, C>, ConfigSource {

    /**
     * Weight of the server export (used for load balancing).
     */
    protected int weight = Constant.DEFAULT_WEIGHT;

    /**
     * Address where the server is exported.
     */
    protected InetSocketAddress exportedAddress;

    /**
     * Configuration for the server.
     */
    protected ServerConfig serverConfig;

    /**
     * The module associated with the exporter.
     */
    protected EffiRpcModule module;

    /**
     * Sets the server configuration for the exporter.
     *
     * @param serverConfig The configuration for the server
     * @return The current builder instance for method chaining
     */
    public C serverConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        return returnThis();
    }

    /**
     * Sets the module for the exporter.
     *
     * @param module The module that will be associated with the exporter
     * @return The current builder instance for method chaining
     */
    public C module(EffiRpcModule module) {
        this.module = module;
        return returnThis();
    }

    /**
     * Sets the weight for the server export (used for load balancing).
     *
     * @param weight Weight value
     * @return This builder
     */
    public C weight(int weight) {
        this.weight = weight;
        return returnThis();
    }

    /**
     * Sets the exported address from a string representation (e.g., "127.0.0.1:8080").
     *
     * @param exportedAddress String representation of the exported address
     * @return This builder
     */
    public C exportedAddress(String exportedAddress) {
        return exportedAddress(NetUtil.toInetSocketAddress(exportedAddress));
    }

    /**
     * Sets the exported address using an IP and port.
     *
     * @param ip   IP address
     * @param port Port number
     * @return This builder
     */
    public C exportedAddress(String ip, int port) {
        if (NetUtil.isValidIP(ip) && NetUtil.isValidPort(port)) {
            return exportedAddress(NetUtil.toAddress(ip, port));
        }
        return returnThis();
    }

    /**
     * Sets the exported address using an {@link InetSocketAddress}.
     *
     * @param address {@link InetSocketAddress} for the exported address
     * @return This builder
     */
    public C exportedAddress(InetSocketAddress address) {
        this.exportedAddress = address;
        return returnThis();
    }

    /**
     * Sets the exported address using a port number and automatically determines the IP address.
     *
     * @param port Port number
     * @return This builder
     * @throws IllegalArgumentException if the port is invalid
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

