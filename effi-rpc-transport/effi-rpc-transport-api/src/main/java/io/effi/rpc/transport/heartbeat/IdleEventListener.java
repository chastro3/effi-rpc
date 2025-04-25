package io.effi.rpc.transport.heartbeat;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.SystemKey;
import io.effi.rpc.event.EventListener;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.endpoint.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listens for {@link IdleEvent} and handles idle timeout logic.
 * If the idle count exceeds the threshold, the associated channel is closed.
 */
public class IdleEventListener implements EventListener<IdleEvent> {

    private static final Logger logger = LoggerFactory.getLogger(IdleEventListener.class);

    @Override
    public void onEvent(IdleEvent event) {
        Channel channel = event.source();
        URL url = channel.url();
        AtomicInteger ideCount = channel.get(KeyConstant.IDLE_COUNT);
        int idleCountThreshold = url.getIntParam(DefaultConfigKeys.IDLE_COUNT_THRESHOLD);
        if (ideCount != null) {
            String enablePrintLog = System.getProperty(SystemKey.PRINT_HEARTBEAT_LOG);
            if (!StringUtil.isBlank(enablePrintLog)
                    && enablePrintLog.equalsIgnoreCase(Boolean.TRUE.toString())) {
                logger.trace("{}[idle count:{},heartbeat interval:{}]", channel, ideCount, idleCountThreshold);
            }
            if (ideCount.get() >= idleCountThreshold) {
                channel.close();
            }
        }
    }
}
