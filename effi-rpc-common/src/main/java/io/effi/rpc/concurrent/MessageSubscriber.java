package io.effi.rpc.concurrent;

import io.effi.rpc.exception.EffiRpcException;

import java.util.concurrent.Flow;

/**
 * Outbound message subscriber backed by JDK Flow with protocol readiness signaling.
 */
public interface MessageSubscriber<T> extends Flow.Subscriber<T> {

    boolean ready();

    void onReady(Runnable callback);

    void cancel(EffiRpcException reason);
}
