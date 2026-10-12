package io.effi.rpc.protocol.grpc.codec;

/**
 * Defines the canonical gRPC status codes.
 */
public enum GrpcStatus {

    OK(0),
    CANCELLED(1),
    UNKNOWN(2),
    INVALID_ARGUMENT(3),
    DEADLINE_EXCEEDED(4),
    NOT_FOUND(5),
    ALREADY_EXISTS(6),
    PERMISSION_DENIED(7),
    RESOURCE_EXHAUSTED(8),
    FAILED_PRECONDITION(9),
    ABORTED(10),
    OUT_OF_RANGE(11),
    UNIMPLEMENTED(12),
    INTERNAL(13),
    UNAVAILABLE(14),
    DATA_LOSS(15),
    UNAUTHENTICATED(16);

    private static final GrpcStatus[] VALUES = values();

    private final int code;

    GrpcStatus(int code) {
        this.code = code;
    }

    /**
     * Resolves the status for the supplied numeric code.
     *
     * @param code numeric status code
     * @return matching status, or {@link #UNKNOWN} when unmatched
     */
    public static GrpcStatus fromCode(int code) {
        for (GrpcStatus status : VALUES) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }

    /**
     * Returns the numeric status code.
     */
    public int code() {
        return code;
    }
}
