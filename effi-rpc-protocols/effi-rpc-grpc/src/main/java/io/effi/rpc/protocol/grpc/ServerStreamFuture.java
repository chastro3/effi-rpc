package io.effi.rpc.protocol.grpc;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Envelope;
import io.effi.rpc.base.ReplyFuture;
import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.util.AssertUtil;

public class ServerStreamFuture extends ReplyFuture {

    private final StreamObserver<Object> responseObserver;

    public ServerStreamFuture(InvocationContext<Envelope.Request, Caller<?>> context, StreamObserver<Object> responseObserver) {
        super(context);
        this.responseObserver = AssertUtil.notNull(responseObserver, "responseObserver");
    }

    public void onData(Object data) {
        responseObserver.onNext(data);
    }

    @Override
    public boolean completed() {
        return false;
    }

    @Override
    public void startTimeout() {

    }

    @Override
    protected void doComplete(Object value) {

    }

    @Override
    protected void doCompleteExceptionally(EffiRpcException e) {
        responseObserver.onCompleted();
    }
}
