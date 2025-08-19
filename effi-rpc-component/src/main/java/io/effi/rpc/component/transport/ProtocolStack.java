package io.effi.rpc.component.transport;

/**
 * Defines transport layer protocols for network communication.
 * <p>
 * Provides an enumeration of supported network protocols including
 * TCP (reliable, connection-oriented) and UDP (unreliable, connectionless).
 */
public enum ProtocolStack {

    TCP("tcp"),

    UDP("udp");

    private final String protocol;

    ProtocolStack(String protocol) {
        this.protocol = protocol;
    }

    public String protocol() {
        return protocol;
    }

}
