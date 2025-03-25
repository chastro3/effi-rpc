package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.transport.InitializedConfig;
import io.effi.rpc.transport.server.NettyServer;

/**
 * Http Server.
 */
public class Http1Server extends NettyServer {

    public Http1Server(InitializedConfig config) {
        super(config);
    }
}
