package io.effi.rpc.util;

import io.vertx.core.Future;

import java.util.concurrent.CompletionException;

/**
 * Utility class for working with Vert.x.
 */
public class VertxUtil {

    /**
     * Await the completion of the future.
     *
     * @param future
     * @param <T>
     * @return
     * @throws CompletionException
     */
    public static <T> T await(Future<T> future) throws Throwable {
        try {
            return future.toCompletionStage().toCompletableFuture().join();
        } catch (CompletionException e) {
            throw e.getCause();
        }
    }
}
