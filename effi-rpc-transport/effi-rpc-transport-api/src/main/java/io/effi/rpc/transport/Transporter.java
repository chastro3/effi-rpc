package io.effi.rpc.transport;

import io.effi.rpc.common.spi.Extensible;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.transport.endpoint.Client;
import io.effi.rpc.transport.endpoint.Server;

/**
 * Creates clients and servers.
 */
@Extensible
public interface Transporter {

    /**
     * Connects to a remote server.
     *
     * @param url    the server URL
     * @param module the associated module
     * @return the connected client
     */
    Client connect(URL url, EffiRpcModule module);

    /**
     * Binds a server to the specified URL.
     *
     * @param url    the server URL
     * @param module the associated module
     * @return the bound server
     */
    Server bind(URL url, EffiRpcModule module);
}

