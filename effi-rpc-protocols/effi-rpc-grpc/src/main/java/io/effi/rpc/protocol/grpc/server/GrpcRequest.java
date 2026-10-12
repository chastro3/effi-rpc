package io.effi.rpc.protocol.grpc.server;

import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.Request;

/**
 * Defines the request URL of one server-side gRPC call.
 */
record GrpcRequest(SmartURL url) implements Request {

    @Override
    public boolean needReply() {
        return true;
    }
}
