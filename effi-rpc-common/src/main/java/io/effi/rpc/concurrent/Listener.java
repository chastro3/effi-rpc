package io.effi.rpc.concurrent;

import io.effi.rpc.trait.Cleanable;

import java.util.function.Consumer;

public interface Listener<T> extends Cleanable {

    void trigger(T event);

    Listener<T> add(Consumer<T> handler);
}