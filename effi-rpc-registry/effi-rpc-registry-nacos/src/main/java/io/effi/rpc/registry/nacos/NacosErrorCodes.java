package io.effi.rpc.registry.nacos;

import io.effi.rpc.exception.ErrorCode;
import io.effi.rpc.exception.ErrorCodeAllocator;

public interface NacosErrorCodes {

    ErrorCodeAllocator NACOS_ERROR_CODE_ALLOCATOR = new ErrorCodeAllocator("nacos_");

    ErrorCode NAMING_SERVICE_CREATE = allocate("Failed to create naming service");

    ErrorCode REGISTER_INSTANCE = allocate("Failed to register instance");

    ErrorCode DEREGISTER_INSTANCE = allocate("Failed to deregister instance");

    ErrorCode LOOKUP_INSTANCE = allocate("Failed to lookup instance");

    private static ErrorCode allocate(String message) {
        return NACOS_ERROR_CODE_ALLOCATOR.next(message);
    }
}
