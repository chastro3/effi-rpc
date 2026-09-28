package io.effi.rpc.transport;

import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.metrics.CalleeMetrics;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.options.SerializationOptions;

/**
 * Provides stateless transport lookup and policy helpers.
 */
public final class TransportSupport {

    public static TransportProtocol findProtocol(Peer peer) {
        if (peer.protocol() instanceof TransportProtocol transportProtocol) {
            return transportProtocol;
        }
        return peer.platform().namedComponent(TransportProtocol.class, peer.protocol().name());
    }

    public static boolean inIOSerialization(Peer peer) {
        Long threshold = peer.option(SerializationOptions.SERIALIZATION_THRESHOLD);
        if (threshold == null || threshold <= 0) {
            return true;
        }
        return averageSerializationTime(peer) < threshold;
    }

    public static boolean inIODeserialization(Peer peer) {
        Long threshold = peer.option(SerializationOptions.DESERIALIZATION_THRESHOLD);
        if (threshold == null || threshold <= 0) {
            return true;
        }
        return averageDeserializationTime(peer) < threshold;
    }

    private static double averageSerializationTime(Peer peer) {
        if (peer instanceof Caller<?> caller) {
            return caller.get(CallerMetrics.GENERIC_KEY).averageSerializationTime().get();
        }
        return peer.get(CalleeMetrics.GENERIC_KEY).averageSerializationTime().get();
    }

    private static double averageDeserializationTime(Peer peer) {
        if (peer instanceof Caller<?> caller) {
            return caller.get(CallerMetrics.GENERIC_KEY).averageDeserializationTime().get();
        }
        return peer.get(CalleeMetrics.GENERIC_KEY).averageDeserializationTime().get();
    }

    private TransportSupport() {
    }
}
