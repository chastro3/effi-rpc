package io.effi.rpc.transport;

import io.effi.rpc.context.Caller;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.metrics.PeerMetrics;
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
        PeerMetrics metrics = PeerMetrics.of(peer);
        return metrics == null || metrics.averageSerializationNanos() < threshold;
    }

    public static boolean inIODeserialization(Peer peer) {
        Long threshold = peer.option(SerializationOptions.DESERIALIZATION_THRESHOLD);
        if (threshold == null || threshold <= 0) {
            return true;
        }
        PeerMetrics metrics = PeerMetrics.of(peer);
        return metrics == null || metrics.averageDeserializationNanos() < threshold;
    }

    private TransportSupport() {
    }
}
