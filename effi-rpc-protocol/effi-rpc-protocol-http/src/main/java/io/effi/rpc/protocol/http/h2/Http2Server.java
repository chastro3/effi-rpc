package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.transport.InitializedConfig;
import io.effi.rpc.transport.server.NettyServer;

/**
 * Http2 Server.
 */
public class Http2Server extends NettyServer {

    public Http2Server(InitializedConfig config) {
        super(config);
    }
}
