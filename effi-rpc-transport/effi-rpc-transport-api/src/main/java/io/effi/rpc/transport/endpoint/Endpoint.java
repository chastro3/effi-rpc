package io.effi.rpc.transport.endpoint;

import io.effi.rpc.component.PlatformSource;
import io.effi.rpc.config.URLSource;
import io.effi.rpc.config.transport.EndpointConfig;
import io.effi.rpc.util.resoruce.Closeable;

import java.net.InetSocketAddress;

/**
 * Represents an endpoint with host, port, and address details.
 */
public interface Endpoint extends URLSource, PlatformSource, Closeable {

    /**
     * Returns host name or IP address.
     */
    String host();

    /**
     * Returns port number.
     */
    int port();

    /**
     * Returns socket address composed of host and port.
     */
    InetSocketAddress socketAddress();

    /**
     * Returns endpoint configuration.
     */
    EndpointConfig config();

    /**
     * Returns address string in "host:port" format.
     */
    default String address() {
        return url().address();
    }
}



