package io.effi.rpc.transport.idle;

import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.component.event.EventListener;
import io.effi.rpc.transport.endpoint.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listens for {@link RefreshIdleCountEvent} and resets the idle count for the associated channel.
 */
public class RefreshIdleCountEventListener implements EventListener<RefreshIdleCountEvent> {

    @Override
    public void onEvent(RefreshIdleCountEvent event) {
        Channel channel = event.source();
        AtomicInteger ideCount = channel.get(KeyConstant.IDLE_COUNT);
        if (ideCount == null) {
            channel.set(KeyConstant.IDLE_COUNT, new AtomicInteger(0));
        } else {
            ideCount.set(0);
        }
    }
}
