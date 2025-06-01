package io.effi.rpc.config.transport;

/**
 * Defines transport layer protocols used for network communication.
 * Typically, includes TCP (reliable, connection-oriented) and UDP (unreliable, connectionless).
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
