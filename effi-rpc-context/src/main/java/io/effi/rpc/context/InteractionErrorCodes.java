package io.effi.rpc.context;

import io.effi.rpc.exception.ErrorCode;
import io.effi.rpc.exception.ErrorCodeAllocator;

public interface InteractionErrorCodes {

    ErrorCodeAllocator CONTEXT_ERROR_CODE_ALLOCATOR = new ErrorCodeAllocator("interaction_");

    ErrorCode SERVICE_CALL_TIMEOUT = allocate("Failed to call remote service: Timeout after '{}' milliseconds,future id is'{}'");

    ErrorCode SERVICE_INSTANCE_NOT_FOUND = allocate("No service instance found for '{}'");

    ErrorCode SERVANT_NOT_FOUND = allocate("Servant not found for '{}' on '{}'");

    ErrorCode SERVANT_INVOCATION_FAILED = allocate("Failed to invoke Servant: '{}'");

    private static ErrorCode allocate(String message) {
        return CONTEXT_ERROR_CODE_ALLOCATOR.next(message);
    }

}
