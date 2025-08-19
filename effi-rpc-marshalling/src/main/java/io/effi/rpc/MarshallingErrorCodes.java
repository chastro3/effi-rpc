package io.effi.rpc;

import io.effi.rpc.exception.ErrorCode;
import io.effi.rpc.exception.ErrorCodeAllocator;

public interface MarshallingErrorCodes {

    ErrorCodeAllocator MARSHALLING_ERROR_CODE_ALLOCATOR = new ErrorCodeAllocator("marshalling_");

    ErrorCode SERIALIZE = allocate("Failed to serialize object '{}'");

    ErrorCode DESERIALIZE = allocate("Failed to deserialize bytes to '{}'");

    ErrorCode ENCODE = allocate("Failed to encode object '{}' from '{}'");

    ErrorCode DECODE = allocate("Failed to decode object '{}' from '{}'");

    ErrorCode COMPRESS = allocate("Failed to compression bytes");

    ErrorCode DECOMPRESS = allocate("Failed to decompress bytes");

    private static ErrorCode allocate(String message) {
        return MARSHALLING_ERROR_CODE_ALLOCATOR.next(message);
    }
}
