package io.effi.rpc.concurrent;

import io.effi.rpc.exception.ErrorCode;
import io.effi.rpc.exception.ErrorCodeAllocator;

/**
 * Defines error codes raised by concurrent operations.
 */
public interface ConcurrentErrorCodes {

    ErrorCodeAllocator ALLOCATOR = new ErrorCodeAllocator("concurrent_");

    ErrorCode TASK_FAILED = allocate("Concurrent task '{}' failed");

    ErrorCode TASK_REJECTED = allocate("Concurrent task '{}' was rejected");

    ErrorCode FUTURE_MAPPER_FAILED = allocate("Future mapper failed: '{}'");

    ErrorCode FUTURE_MAPPER_RETURNED_NULL = allocate("Future mapper returned null");

    ErrorCode INTERRUPTED = allocate("Concurrent operation was interrupted: '{}'");

    private static ErrorCode allocate(String message) {
        return ALLOCATOR.next(message);
    }
}
