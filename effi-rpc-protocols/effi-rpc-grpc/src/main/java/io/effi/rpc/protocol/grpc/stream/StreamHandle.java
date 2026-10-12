package io.effi.rpc.protocol.grpc.stream;

import io.effi.rpc.concurrent.MessagePublisher;
import io.effi.rpc.concurrent.MessageSubscriber;
import io.effi.rpc.util.AssertUtil;

/**
 * Defines the inbound publisher and outbound subscriber pair of one gRPC stream.
 *
 * @param <IN>  inbound message type
 * @param <OUT> outbound message type
 */
public record StreamHandle<IN, OUT>(MessagePublisher<IN> inbound, MessageSubscriber<OUT> outbound) {

    public StreamHandle(MessagePublisher<IN> inbound, MessageSubscriber<OUT> outbound) {
        this.inbound = AssertUtil.notNull(inbound, "inbound");
        this.outbound = AssertUtil.notNull(outbound, "outbound");
    }
}
