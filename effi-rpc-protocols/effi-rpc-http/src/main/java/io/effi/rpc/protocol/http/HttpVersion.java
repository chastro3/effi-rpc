package io.effi.rpc.protocol.http;

import io.effi.rpc.protocol.http.support.HttpHeaders;

import java.util.Map;

/**
 * Defines the version-specific HTTP header operations used by the protocol layer.
 */
public interface HttpVersion {

    /**
     * Returns the protocol version name.
     */
    String name();

    /**
     * Creates an empty header collection for this version.
     */
    HttpHeaders newHeaders();

    /**
     * Creates a header collection initialized with the supplied entries.
     *
     * @param headers initial header entries
     */
    HttpHeaders newHeaders(Iterable<? extends Map.Entry<? extends CharSequence, ? extends CharSequence>> headers);

    /**
     * Returns the URL scheme used by this protocol version.
     */
    default String schema() {
        return "http";
    }

}
