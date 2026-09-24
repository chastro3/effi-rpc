package io.effi.rpc.transport;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.context.CallFutureRegistry;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.trait.Closeable;
import io.effi.rpc.transport.endpoint.Channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static io.effi.rpc.annotation.component.ScopedComponent.Kind.SINGLE;
import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Platform-scoped binding index from channels to in-flight call ids.
 */
@ScopedComponent(scope = PLATFORM, kind = SINGLE)
public final class ChannelCallBindings implements Closeable {

    private final CallFutureRegistry callFutureRegistry;

    private final AtomicBoolean active = new AtomicBoolean(true);

    private final ConcurrentMap<Channel, Set<Long>> channelCalls = new ConcurrentHashMap<>();

    public ChannelCallBindings(CallFutureRegistry callFutureRegistry) {
        this.callFutureRegistry = callFutureRegistry;
    }

    public synchronized boolean bind(long callId, Channel channel) {
        if (!active.get() || !channel.active() || callFutureRegistry.lookup(callId) == null) {
            return false;
        }
        channelCalls.computeIfAbsent(channel, ignored -> ConcurrentHashMap.newKeySet()).add(callId);
        callFutureRegistry.onTerminate(callId, () -> unbind(callId, channel));
        return true;
    }

    public synchronized void unbind(long callId, Channel channel) {
        Set<Long> callIds = channelCalls.get(channel);
        if (callIds == null) {
            return;
        }
        callIds.remove(callId);
        if (callIds.isEmpty()) {
            channelCalls.remove(channel, callIds);
        }
    }

    public void cancelChannel(Channel channel, EffiRpcException reason) {
        Set<Long> callIds;
        synchronized (this) {
            callIds = channelCalls.remove(channel);
        }
        if (callIds == null || callIds.isEmpty()) {
            return;
        }
        callIds.forEach(callId -> callFutureRegistry.cancel(callId, reason));
    }

    public int size() {
        return channelCalls.size();
    }

    @Override
    public boolean active() {
        return active.get();
    }

    @Override
    public void close() {
        Map<Channel, Set<Long>> bindings;
        synchronized (this) {
            if (!active.compareAndSet(true, false)) {
                return;
            }
            bindings = new HashMap<>(channelCalls);
            channelCalls.clear();
        }
        EffiRpcException reason = TransportErrorCodes.CALL_BINDINGS_CLOSED.fail();
        Set<Long> callIds = new HashSet<>();
        bindings.values().forEach(callIds::addAll);
        callIds.forEach(callId -> callFutureRegistry.cancel(callId, reason));
    }
}
