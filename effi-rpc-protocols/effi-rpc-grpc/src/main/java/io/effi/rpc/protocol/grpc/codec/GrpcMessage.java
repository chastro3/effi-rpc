package io.effi.rpc.protocol.grpc.codec;

/**
 * Defines one framed gRPC message.
 */
public record GrpcMessage(boolean compressed, byte[] payload) {
}
