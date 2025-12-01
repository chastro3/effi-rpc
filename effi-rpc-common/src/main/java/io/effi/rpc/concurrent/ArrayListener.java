package io.effi.rpc.concurrent;

import io.effi.rpc.util.AssertUtil;

import java.util.Arrays;
import java.util.function.Consumer;

public final class ArrayListener<T> implements Listener<T> {

    public volatile Consumer<T>[] handlers;
    private volatile int size;

    @SuppressWarnings("unchecked")
    public ArrayListener(int initialCapacity) {
        if (initialCapacity <= 0) initialCapacity = 4;
        handlers = (Consumer<T>[]) new Consumer[initialCapacity];
    }

    public ArrayListener() {
        this(4);
    }

    @Override
    public void trigger(T event) {
        int n = size;
        Consumer<T>[] a = handlers;
        for (int i = 0; i < n; i++) {
            a[i].accept(event);
        }
    }

    @Override
    public Listener<T> add(Consumer<T> handler) {
        AssertUtil.notNull(handler, "handler");
        synchronized (this) {
            Consumer<T>[] a = handlers;
            int n = size;
            if (n == a.length) {
                a = Arrays.copyOf(a, n + (n >> 1) + 1);
                handlers = a;
            }
            a[n] = handler;
            size = n + 1;
            return this;
        }
    }

    public void clear() {
        Consumer<T>[] a = handlers;
        int n = size;
        for (int i = 0; i < n; i++) a[i] = null;
        size = 0;
    }

    public int size() { return size; }
}
