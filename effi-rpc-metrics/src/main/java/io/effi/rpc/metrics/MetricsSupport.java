package io.effi.rpc.metrics;

import io.effi.rpc.base.context.InvocationContext;
import io.effi.rpc.metrics.constant.MetricsKey;

/**
 * Utility for recording and retrieving invocation metrics.
 */
public class MetricsSupport {

    /**
     * Records the start time of an invocation context in nanoseconds.
     *
     * @param context The invocation context to record the start time for.
     * @return The recorded start time in nanoseconds.
     */
    public static long recordStartTime(InvocationContext<?, ?> context) {
        long start = System.nanoTime();
        context.set(MetricsKey.START_TIME, start);
        return start;
    }

    /**
     * Records the end time of an invocation context in nanoseconds.
     *
     * @param context The invocation context to record the end time for.
     * @return The recorded end time in nanoseconds.
     */
    public static long recordEndTime(InvocationContext<?, ?> context) {
        long end = System.nanoTime();
        context.set(MetricsKey.END_TIME, end);
        return end;
    }

    /**
     * Records the start time of serialization in nanoseconds.
     *
     * @param context The invocation context to record the start time for.
     * @return The recorded serialization end time in nanoseconds.
     */
    public static long recordSerializeStartTime(InvocationContext<?, ?> context) {
        long start = System.nanoTime();
        context.set(MetricsKey.SERIALIZE_START_TIME, start);
        return start;
    }

    /**
     * Records the end time of serialization in nanoseconds.
     *
     * @param context The invocation context to record the end time for.
     * @return The recorded serialization end time in nanoseconds.
     */
    public static long recordSerializeEndTime(InvocationContext<?, ?> context) {
        long end = System.nanoTime();
        context.set(MetricsKey.SERIALIZE_END_TIME, end);
        return end;
    }

    /**
     * Records the start time of deserialization in nanoseconds.
     *
     * @param context The invocation context to record the start time for.
     * @return The recorded deserialization end time in nanoseconds.
     */
    public static long recordDeserializeStartTime(InvocationContext<?, ?> context) {
        long start = System.nanoTime();
        context.set(MetricsKey.DESERIALIZE_START_TIME, start);
        return start;
    }

    /**
     * Records the end time of deserialization in nanoseconds.
     *
     * @param context The invocation context to record the end time for.
     * @return The recorded deserialization end time in nanoseconds.
     */
    public static long recordDeserializeEndTime(InvocationContext<?, ?> context) {
        long end = System.nanoTime();
        context.set(MetricsKey.DESERIALIZE_END_TIME, end);
        return end;
    }

    /**
     * Retrieves the recorded start time of an invocation context.
     *
     * @param context The invocation context to retrieve the start time for.
     * @return The start time in nanoseconds.
     */
    public static long getStartTime(InvocationContext<?, ?> context) {
        return context.get(MetricsKey.START_TIME);
    }
}

