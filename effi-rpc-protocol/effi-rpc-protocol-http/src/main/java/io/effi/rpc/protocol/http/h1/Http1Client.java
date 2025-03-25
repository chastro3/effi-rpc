package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.transport.InitializedConfig;
import io.effi.rpc.transport.client.NettyPoolClient;

/**
 * Http Client.
 */
public class Http1Client extends NettyPoolClient {

    public Http1Client(InitializedConfig config) {
        super(config);
    }

}
