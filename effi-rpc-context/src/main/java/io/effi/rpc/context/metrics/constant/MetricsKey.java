package io.effi.rpc.context.metrics.constant;

import io.effi.rpc.util.GenericKey;

/**
 * Metrics Key.
 */
public class MetricsKey {

    public static final GenericKey<Long> START_TIME = GenericKey.valueOf("startTime");

    public static final GenericKey<Long> END_TIME = GenericKey.valueOf("endTime");

    public static final GenericKey<Long> SERIALIZE_START_TIME = GenericKey.valueOf("serializeStartTime");

    public static final GenericKey<Long> SERIALIZE_END_TIME = GenericKey.valueOf("serializeEndTime");

    public static final GenericKey<Long> DESERIALIZE_START_TIME = GenericKey.valueOf("deserializeStartTime");

    public static final GenericKey<Long> DESERIALIZE_END_TIME = GenericKey.valueOf("deserializeEndTime");

}
