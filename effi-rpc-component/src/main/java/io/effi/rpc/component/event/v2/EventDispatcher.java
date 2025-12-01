package io.effi.rpc.component.event.v2;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.trait.Closeable;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public interface EventDispatcher extends Closeable, ScopedPlatform.Supplier {

    <E extends Event> void registerHandler(Class<E> eventType, EventHandler<E> handler);

    <E extends Event> void removeHandler(Class<E> eventType);

    <E extends Event> void publish(E event);

    void start();

    int pendingCount();
}