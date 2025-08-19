package io.effi.rpc.context.metrics;

import io.effi.rpc.context.CallContext;
import io.effi.rpc.context.metrics.constant.MetricsKey;

/**
 * Utility for recording and retrieving invocation metrics.
 */
public class MetricsSupport {

    /**
     * Records the start time of an invocation context in nanoseconds.
     *
     * @param context the invocation context to record the start time for.
     * @return the recorded start time in nanoseconds.
     */
    public static long recordStartTime(CallContext<?, ?> context) {
        long start = System.nanoTime();
        context.set(MetricsKey.START_TIME, start);
        return start;
    }

    /**
     * Records the end time of an invocation context in nanoseconds.
     *
     * @param context the invocation context to record the end time for.
     * @return the recorded end time in nanoseconds.
     */
    public static long recordEndTime(CallContext<?, ?> context) {
        long end = System.nanoTime();
        context.set(MetricsKey.END_TIME, end);
        return end;
    }

    /**
     * Records the start time of serialization in nanoseconds.
     *
     * @param context the invocation context to record the start time for.
     * @return the recorded serialization end time in nanoseconds.
     */
    public static long recordSerializeStartTime(CallContext<?, ?> context) {
        long start = System.nanoTime();
        context.set(MetricsKey.SERIALIZE_START_TIME, start);
        return start;
    }

    /**
     * Records the end time of serialization in nanoseconds.
     *
     * @param context the invocation context to record the end time for.
     * @return the recorded serialization end time in nanoseconds.
     */
    public static long recordSerializeEndTime(CallContext<?, ?> context) {
        long end = System.nanoTime();
        context.set(MetricsKey.SERIALIZE_END_TIME, end);
        return end;
    }

    /**
     * Records the start time of deserialization in nanoseconds.
     *
     * @param context the invocation context to record the start time for.
     * @return the recorded deserialization end time in nanoseconds.
     */
    public static long recordDeserializeStartTime(CallContext<?, ?> context) {
        long start = System.nanoTime();
        context.set(MetricsKey.DESERIALIZE_START_TIME, start);
        return start;
    }

    /**
     * Records the end time of deserialization in nanoseconds.
     *
     * @param context the invocation context to record the end time for.
     * @return the recorded deserialization end time in nanoseconds.
     */
    public static long recordDeserializeEndTime(CallContext<?, ?> context) {
        long end = System.nanoTime();
        context.set(MetricsKey.DESERIALIZE_END_TIME, end);
        return end;
    }

    /**
     * Retrieves the recorded start time of an invocation context.
     *
     * @param context the invocation context to retrieve the start time for.
     * @return the start time in nanoseconds.
     */
    public static long getStartTime(CallContext<?, ?> context) {
        return context.get(MetricsKey.START_TIME);
    }
}

