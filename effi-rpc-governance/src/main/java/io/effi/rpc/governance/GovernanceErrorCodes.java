package io.effi.rpc.governance;

import io.effi.rpc.exception.ErrorCode;
import io.effi.rpc.exception.ErrorCodeAllocator;

/**
 * Defines error codes raised during service governance.
 */
public interface GovernanceErrorCodes {

    ErrorCodeAllocator GOVERNANCE_ERROR_CODE_ALLOCATOR = new ErrorCodeAllocator("governance_");

    ErrorCode SERVICE_INSTANCE_NOT_FOUND = allocate("No service instance found for '{}'");

    ErrorCode ROUTE_NOT_MATCHED = allocate("No service instance matched router for '{}'");

    ErrorCode HASH_KEY_REQUIRED = allocate("Consistent-hash load balancing requires the 'hashKey' context attribute");

    private static ErrorCode allocate(String message) {
        return GOVERNANCE_ERROR_CODE_ALLOCATOR.next(message);
    }
}
