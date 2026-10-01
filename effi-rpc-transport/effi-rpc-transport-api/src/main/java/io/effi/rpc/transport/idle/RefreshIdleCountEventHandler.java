package io.effi.rpc.transport.idle;

import io.effi.rpc.component.event.EventHandler;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.transport.endpoint.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles {@link RefreshIdleCountEvent} by resetting a channel idle counter.
 */
public class RefreshIdleCountEventHandler implements EventHandler<RefreshIdleCountEvent> {

    @Override
    public void onEvent(RefreshIdleCountEvent event) {
        Channel channel = event.channel();
        AtomicInteger idleCount = channel.get(KeyConstant.IDLE_COUNT);
        if (idleCount == null) {
            channel.set(KeyConstant.IDLE_COUNT, new AtomicInteger(0));
        } else {
            idleCount.set(0);
        }
    }
}
