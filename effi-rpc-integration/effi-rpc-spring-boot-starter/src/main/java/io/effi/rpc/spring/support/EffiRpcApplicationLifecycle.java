package io.effi.rpc.spring.support;

import io.effi.rpc.core.EffiRpcBootstrap;
import io.effi.rpc.concurrent.Deadline;
import io.effi.rpc.concurrent.Result;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

/**
 * Manages the RPC application lifecycle as the outer Spring lifecycle.
 */
public final class EffiRpcApplicationLifecycle implements SmartLifecycle {

    private static final long START_TIMEOUT_SECONDS = 30L;

    private final EffiRpcBootstrap bootstrap;

    private volatile boolean running;

    public EffiRpcApplicationLifecycle(EffiRpcBootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public void start() {
        try {
            Result<Void> result = bootstrap.start()
                    .await(Deadline.after(START_TIMEOUT_SECONDS, TimeUnit.SECONDS));
            if (result.failed()) {
                throw new CompletionException(result.cause());
            }
            running = true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CompletionException(e);
        }
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }
        try {
            bootstrap.stop();
        } finally {
            running = false;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }
}
