package io.effi.rpc.concurrent;

import java.util.concurrent.Flow;

/**
 * Inbound message stream backed by JDK Flow.
 */
public interface MessagePublisher<T> extends Flow.Publisher<T> {
}
