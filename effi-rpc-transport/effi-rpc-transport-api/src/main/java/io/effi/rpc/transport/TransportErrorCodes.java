package io.effi.rpc.transport;

import io.effi.rpc.exception.ErrorCode;
import io.effi.rpc.exception.ErrorCodeAllocator;

public interface TransportErrorCodes {

    ErrorCodeAllocator TRANSPORT_ERROR_CODE_ALLOCATOR = new ErrorCodeAllocator("transport_");

    ErrorCode BIND = allocate("Failed to bind server to '{}'");
    ErrorCode CONNECT = allocate("Failed to connect to '{}'");
    ErrorCode FETCH_CHANNEL = allocate("Failed to fetch channel to '{}' over ({}) protocol");
    ErrorCode CLOSE_CHANNEL = allocate("Failed to close remote channel to '{}'");
    ErrorCode CHANNEL_WRITE = allocate("Failed to write data to channel '{}'");
    ErrorCode CHANNEL_READ = allocate("Failed to read data from channel '{}'");
    ErrorCode CLOSE_SERVER = allocate("Failed to close server at '{}'");
    ErrorCode ENCODE = allocate("Failed to encode object '{}' from '{}'");
    ErrorCode DECODE = allocate("Failed to decode object '{}' from '{}'");

    private static ErrorCode allocate(String message) {
        return TRANSPORT_ERROR_CODE_ALLOCATOR.next(message);
    }
}
