package io.effi.rpc.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntUnaryOperator;
import java.util.function.LongUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * Provides atomic operations.
 */
public final class AtomicUtil {

    /**
     * Atomically updates the AtomicLong value using the provided update function.
     */
    public static long updateAtomicLong(AtomicLong atomicLong, LongUnaryOperator updateFunction) {
        long prevValue, newValue;
        do {
            prevValue = atomicLong.get();
            newValue = updateFunction.applyAsLong(prevValue);
        } while (!atomicLong.compareAndSet(prevValue, newValue));
        return newValue;
    }

    /**
     * Atomically updates the AtomicInteger value using the provided update function.
     */
    public static int updateAtomicInteger(AtomicInteger atomicInteger, IntUnaryOperator updateFunction) {
        int prevValue, newValue;
        do {
            prevValue = atomicInteger.get();
            newValue = updateFunction.applyAsInt(prevValue);
        } while (!atomicInteger.compareAndSet(prevValue, newValue));
        return newValue;
    }

    /**
     * Atomically updates the AtomicReference value using the provided update function.
     */
    public static <T> void updateAtomicReference(AtomicReference<T> atomicReference, UnaryOperator<T> updateFunction) {
        T prevValue, newValue;
        do {
            prevValue = atomicReference.get();
            newValue = updateFunction.apply(prevValue);
        } while (!atomicReference.compareAndSet(prevValue, newValue));
    }

    private AtomicUtil() {
    }
}

