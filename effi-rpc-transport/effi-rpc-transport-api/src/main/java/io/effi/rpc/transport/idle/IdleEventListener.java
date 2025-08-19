package io.effi.rpc.transport.idle;

import io.effi.rpc.component.event.EventListener;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.SystemKeys;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.util.StringUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Listens for {@link IdleEvent} and handles idle timeout logic.
 * Closes the associated channel if the idle count exceeds the threshold.
 */
public class IdleEventListener implements EventListener<IdleEvent> {

    private static final Logger logger = LoggerFactory.getLogger(IdleEventListener.class);

    @Override
    public void onEvent(IdleEvent event) {
        Channel channel = event.source();
        EndpointConfig config = channel.endpoint().config();
        AtomicInteger ideCount = channel.get(KeyConstant.IDLE_COUNT);
        int idleCountThreshold = config.getConfig(ConfigNames.IDLE_COUNT_THRESHOLD);
        if (ideCount != null) {
            String enablePrintLog = System.getProperty(SystemKeys.PRINT_HEARTBEAT_LOG);
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
