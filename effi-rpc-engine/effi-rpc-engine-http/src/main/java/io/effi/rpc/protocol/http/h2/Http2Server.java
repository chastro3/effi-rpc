package io.effi.rpc.protocol.http.h2;

import io.effi.rpc.protocol.InitializedConfig;
import io.effi.rpc.protocol.server.NettyServer;

/**
 * Http2 Server.
 */
public class Http2Server extends NettyServer {

    public Http2Server(InitializedConfig config) {
        super(config);
    }
}
