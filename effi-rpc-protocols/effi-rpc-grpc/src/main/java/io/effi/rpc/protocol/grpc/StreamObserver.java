package io.effi.rpc.protocol.grpc;

/**
 * Handles asynchronous stream of messages for gRPC calls,
 * providing callbacks for incoming messages, errors, and completion events.
 *
 * @param <M> the type of messages handled by this observer
 */
public interface StreamObserver<M> {

    /**
     * Receives a new incoming message from the stream.
     *
     * @param value the message sent from the remote peer
     */
    void onNext(M value);

    /**
     * Receives a terminal error from the stream.
     *
     * @param e the throwable error occurred
     */
    void onError(Throwable e);

    /**
     * Receives a notification of stream completion.
     */
    void onCompleted();
}
