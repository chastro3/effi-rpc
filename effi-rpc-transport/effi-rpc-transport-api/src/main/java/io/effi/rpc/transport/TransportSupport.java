package io.effi.rpc.transport;

import io.effi.rpc.component.serialization.options.SerializationOptions;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.metrics.PeerMetrics;
import io.effi.rpc.context.metrics.ServantMetrics;

/**
 * Provides stateless transport lookup and policy helpers.
 */
public final class TransportSupport {

    private TransportSupport() {
    }

    /**
     * Resolves the transport protocol for the supplied peer.
     *
     * @param peer call peer
     * @return transport protocol
     */
    public static TransportProtocol findProtocol(Peer peer) {
        if (peer.protocol() instanceof TransportProtocol transportProtocol) {
            return transportProtocol;
        }
        return peer.platform().namedComponent(TransportProtocol.class, peer.protocol().name());
    }

    /**
     * Indicates whether serialization may run on the IO thread.
     *
     * @param peer call peer
     * @return {@code true} when serialization runs on the IO thread
     */
    public static boolean inIOSerialization(Peer peer) {
        Long threshold = peer.option(SerializationOptions.SERIALIZATION_THRESHOLD);
        if (threshold == null || threshold <= 0) {
            return true;
        }
        PeerMetrics metrics = metrics(peer);
        return metrics == null || metrics.averageSerializationNanos() < threshold;
    }

    // Looks up metrics carried by the peer on either side of the call.
    private static PeerMetrics metrics(Peer peer) {
        if (peer == null) {
            return null;
        }
        PeerMetrics callerMetrics = peer.get(CallerMetrics.KEY);
        return callerMetrics == null ? peer.get(ServantMetrics.KEY) : callerMetrics;
    }

    /**
     * Indicates whether deserialization may run on the IO thread.
     *
     * @param peer call peer
     * @return {@code true} when deserialization runs on the IO thread
     */
    public static boolean inIODeserialization(Peer peer) {
        Long threshold = peer.option(SerializationOptions.DESERIALIZATION_THRESHOLD);
        if (threshold == null || threshold <= 0) {
            return true;
        }
        PeerMetrics metrics = metrics(peer);
        return metrics == null || metrics.averageDeserializationNanos() < threshold;
    }

}
