package io.effi.rpc.transport.endpoint;

import io.effi.rpc.common.extension.resoruce.Closeable;
import io.effi.rpc.common.url.URLSource;
import io.effi.rpc.contract.module.ModuleSource;

import java.net.InetSocketAddress;

/**
 * Endpoint with host, port, and address information.
 */
public interface Endpoint extends URLSource, ModuleSource, Closeable {

    /**
     * Returns the host name or IP address of this endpoint.
     *
     * @return the host name or IP address as a string
     */
    String host();

    /**
     * Returns the port number associated with this endpoint.
     *
     * @return the port number
     */
    int port();

    /**
     * Returns the {@link InetSocketAddress} representing the address of this endpoint,
     * which includes both the host and port.
     *
     * @return the {@link InetSocketAddress} of this endpoint
     */
    InetSocketAddress inetSocketAddress();

    /**
     * Returns the address string of this endpoint in the form of "host:port".
     *
     * @return the address string of this endpoint
     */
    default String address() {
        return url().address();
    }

}
