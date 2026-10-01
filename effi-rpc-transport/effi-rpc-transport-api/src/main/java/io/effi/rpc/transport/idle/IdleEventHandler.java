package io.effi.rpc.transport.idle;

import io.effi.rpc.component.event.EventHandler;
import io.effi.rpc.component.transport.EndpointConfig;
import io.effi.rpc.component.transport.options.TransportOptions;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.constant.SystemKeys;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.transport.endpoint.Channel;
import io.effi.rpc.util.StringUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Handles {@link IdleEvent} by closing channels that exceed the idle threshold.
 */
public class IdleEventHandler implements EventHandler<IdleEvent> {

    private static final Logger logger = LoggerFactory.getLogger(IdleEventHandler.class);

    @Override
    public void onEvent(IdleEvent event) {
        Channel channel = event.channel();
        EndpointConfig config = channel.endpoint().config();
        AtomicInteger idleCount = channel.get(KeyConstant.IDLE_COUNT);
        int idleCountThreshold = config.option(TransportOptions.IDLE_COUNT_THRESHOLD);
        if (idleCount == null) {
            return;
        }
        String enablePrintLog = System.getProperty(SystemKeys.PRINT_HEARTBEAT_LOG);
        if (!StringUtil.isBlank(enablePrintLog)
                && enablePrintLog.equalsIgnoreCase(Boolean.TRUE.toString())) {
            logger.trace("{}[idle count:{},heartbeat interval:{}]", channel, idleCount, idleCountThreshold);
        }
        if (idleCount.get() >= idleCountThreshold) {
            channel.close();
        }
    }
}
