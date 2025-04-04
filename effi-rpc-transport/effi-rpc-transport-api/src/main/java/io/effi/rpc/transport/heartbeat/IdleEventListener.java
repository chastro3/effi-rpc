package io.effi.rpc.transport.heartbeat;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.constant.SystemKey;
import io.effi.rpc.common.event.EventListener;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.transport.endpoint.Channel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Close idle connections according to core {@link ClientConfig#spareCloseTimes} to reduce resource waste.
 */
public class IdleEventListener implements EventListener<IdleEvent> {

    private static final Logger logger = LoggerFactory.getLogger(IdleEventListener.class);

    @Override
    public void onEvent(IdleEvent event) {
        Channel channel = event.source();
        URL url = channel.url();
        AtomicInteger ideCount = channel.get(KeyConstant.IDLE_COUNT);
        int idleCountThreshold = url.getIntParam(KeyConstant.IDLE_COUNT_THRESHOLD, Constant.DEFAULT_IDLE_COUNT_THRESHOLD);
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
