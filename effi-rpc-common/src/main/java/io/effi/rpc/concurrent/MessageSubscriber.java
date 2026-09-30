package io.effi.rpc.concurrent;

import io.effi.rpc.exception.EffiRpcException;

import java.util.concurrent.Flow;

/**
 * Defines an outbound message subscriber backed by JDK Flow with protocol readiness signaling.
 */
public interface MessageSubscriber<T> extends Flow.Subscriber<T> {

    /**
     * Indicates whether the subscriber is ready to receive messages.
     */
    boolean ready();

    /**
     * Registers a callback invoked when the subscriber becomes ready.
     *
     * @param callback readiness callback
     */
    void onReady(Runnable callback);

    /**
     * Cancels the subscription with a protocol error.
     *
     * @param reason cancellation reason
     */
    void cancel(EffiRpcException reason);
}
