package io.effi.rpc.protocol.http.h1;

import io.effi.rpc.protocol.InitializedConfig;
import io.effi.rpc.protocol.client.NettyPoolClient;

/**
 * Http Client.
 */
public class Http1Client extends NettyPoolClient {

    public Http1Client(InitializedConfig config) {
        super(config);
    }

}
