package io.effi.rpc.protocol.grpc.codec;

/**
 * Defines gRPC header and trailer names.
 */
public final class GrpcHeaders {

    public static final String CONTENT_TYPE = "content-type";

    public static final String CONTENT_TYPE_VALUE = "application/grpc";

    public static final String TE = "te";

    public static final String TE_VALUE = "trailers";

    public static final String STATUS = "grpc-status";

    public static final String MESSAGE = "grpc-message";

    public static final String ENCODING = "grpc-encoding";

    public static final String ACCEPT_ENCODING = "grpc-accept-encoding";

    public static final String TIMEOUT = "grpc-timeout";

    private GrpcHeaders() {
    }
}
