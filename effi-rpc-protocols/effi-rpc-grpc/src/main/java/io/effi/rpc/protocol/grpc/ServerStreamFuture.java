package io.effi.rpc.protocol.grpc;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.ReplyFuture;
import io.effi.rpc.util.AssertUtil;

public class ServerStreamFuture extends ReplyFuture {

    private final StreamObserver<Object> responseObserver;

    public ServerStreamFuture(CallContext<Request, Caller<?>> context, StreamObserver<Object> responseObserver) {
        super(context);
        this.responseObserver = AssertUtil.notNull(responseObserver, "responseObserver");
    }

    public void onData(Object data) {
        responseObserver.onNext(data);
    }
}
