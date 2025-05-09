package io.effi.rpc.transport.endpoint;

import io.effi.rpc.config.URLSource;
import io.effi.rpc.contract.module.ModuleSource;
import io.effi.rpc.util.resoruce.Closeable;

import java.net.InetSocketAddress;

/**
 * Represents an endpoint with host, port, and address information.
 */
public interface Endpoint extends URLSource, ModuleSource, Closeable {

    /**
     * Returns the host name or IP address.
     */
    String host();

    /**
     * Returns the port number.
     */
    int port();

    /**
     * Returns the socket address composed of host and port.
     */
    InetSocketAddress socketAddress();

    /**
     * Returns the full address string in "host:port" format.
     */
    default String address() {
        return url().address();
    }
}


