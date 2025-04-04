package io.effi.rpc.transport.heartbeat;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.event.EventListener;
import io.effi.rpc.transport.endpoint.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * The count is refreshed when the current channel information is transmitted.
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
