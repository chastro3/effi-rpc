package io.effi.rpc.protocol.grpc.codec;

import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;

/**
 * Provides mapping between framework failures and gRPC status values.
 */
public final class GrpcStatusMapper {

    private GrpcStatusMapper() {
    }

    /**
     * Resolves the gRPC status for the supplied failure.
     *
     * @param cause terminal failure
     * @return matching gRPC status
     */
    public static GrpcStatus statusOf(EffiRpcException cause) {
        if (cause == null) {
            return GrpcStatus.OK;
        }
        if (cause.errorCode() == PredefinedErrorCode.DEADLINE_EXCEEDED) {
            return GrpcStatus.DEADLINE_EXCEEDED;
        }
        if (cause.errorCode() == PredefinedErrorCode.CALL_CANCELLED) {
            return GrpcStatus.CANCELLED;
        }
        if (cause.errorCode() == PredefinedErrorCode.SERVICE_UNAVAILABLE
                || cause.errorCode() == InteractionErrorCodes.SERVER_OVERLOADED) {
            return GrpcStatus.UNAVAILABLE;
        }
        if (cause.errorCode() == InteractionErrorCodes.SERVANT_NOT_FOUND) {
            return GrpcStatus.UNIMPLEMENTED;
        }
        return GrpcStatus.INTERNAL;
    }

    /**
     * Resolves the framework failure for the supplied gRPC status.
     *
     * @param status  gRPC status
     * @param message server-provided failure message
     * @return matching framework failure
     */
    public static EffiRpcException toException(GrpcStatus status, String message) {
        String detail = message == null ? status.name() : message;
        return switch (status) {
            case DEADLINE_EXCEEDED -> PredefinedErrorCode.DEADLINE_EXCEEDED.fail(detail);
            case CANCELLED -> PredefinedErrorCode.CALL_CANCELLED.fail(detail);
            case UNAVAILABLE -> PredefinedErrorCode.SERVICE_UNAVAILABLE.fail(detail);
            case UNIMPLEMENTED -> InteractionErrorCodes.SERVANT_NOT_FOUND.fail(detail);
            default -> InteractionErrorCodes.SERVANT_INVOCATION_FAILED.fail(detail);
        };
    }
}
