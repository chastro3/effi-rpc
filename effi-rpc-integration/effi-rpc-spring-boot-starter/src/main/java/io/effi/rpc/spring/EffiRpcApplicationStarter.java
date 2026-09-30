package io.effi.rpc.spring;

import io.effi.rpc.boot.EffiRpcBootstrap;
import io.effi.rpc.concurrent.Deadline;
import io.effi.rpc.concurrent.Result;
import org.springframework.context.SmartLifecycle;

import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

public class EffiRpcApplicationStarter implements SmartLifecycle {

    private static final long START_TIMEOUT_SECONDS = 30L;

    private final EffiRpcBootstrap bootstrap;

    private volatile boolean running;

    public EffiRpcApplicationStarter(EffiRpcBootstrap bootstrap) {
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
        bootstrap.stop();
        running = false;
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
