package io.effi.rpc.event;

import io.effi.rpc.util.AbstractAttributes;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract implementation of {@link Event}.
 */
public abstract class AbstractEvent<S> extends AbstractAttributes implements Event<S> {

    protected S data;

    protected AtomicBoolean propagation = new AtomicBoolean(true);

    protected AbstractEvent() {
    }

    protected AbstractEvent(S data) {
        this.data = data;
    }

    @Override
    public S source() {
        return data;
    }

    @Override
    public void source(S source) {
        this.data = source;
    }

    @Override
    public boolean stopPropagation() {
        return propagation.compareAndSet(true, false);
    }

    @Override
    public boolean allowPropagation() {
        return propagation.get();
    }

}
