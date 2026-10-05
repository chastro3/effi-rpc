package io.effi.rpc.registry.consul;

import io.effi.rpc.exception.ErrorCode;
import io.effi.rpc.exception.ErrorCodeAllocator;

/**
 * Defines Consul registry error codes.
 */
public interface ConsulErrorCodes {

    ErrorCodeAllocator ALLOCATOR = new ErrorCodeAllocator("consul_");

    ErrorCode OPERATION_FAILED = allocate("Consul operation failed: '{}'");

    private static ErrorCode allocate(String message) {
        return ALLOCATOR.next(message);
    }
}
