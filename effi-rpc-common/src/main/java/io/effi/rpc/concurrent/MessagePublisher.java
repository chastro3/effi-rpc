package io.effi.rpc.concurrent;

import java.util.concurrent.Flow;

/**
 * Defines an inbound message stream backed by JDK Flow.
 */
public interface MessagePublisher<T> extends Flow.Publisher<T> {
}
