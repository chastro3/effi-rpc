package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.transport.netty.NettyEndpointConfig;
import io.effi.rpc.transport.netty.NettyServer;

/**
 * Http Server.
 */
public class Http1Server extends NettyServer {

    public Http1Server(NettyEndpointConfig config) {
        super(config);
    }
}
