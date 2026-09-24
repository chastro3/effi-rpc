package io.effi.rpc.exception;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class EffiRpcExceptionTest {

    @Test
    void metadataIsImmutableAndPreservedByCompletionException() {
        EffiRpcException exception = PredefinedErrorCode.COMMON
                .fail("failed")
                .withMetadata(Map.of("grpc-status", "13"));

        assertEquals("13", exception.metadata().get("grpc-status"));
        CompletionException completion = exception.toCompletionException();
        assertSame(exception, completion.getCause());
    }
}
