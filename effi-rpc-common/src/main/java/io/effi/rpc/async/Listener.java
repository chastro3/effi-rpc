package io.effi.rpc.async;

import io.effi.rpc.util.resoruce.Cleanable;

import java.util.function.Consumer;

public interface Listener<T> extends Cleanable {

    void trigger(T event);

    Listener<T> add(Consumer<T> handler);
}