package io.effi.rpc.protocol.event;

import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.constant.SystemKey;
import io.effi.rpc.common.event.EventListener;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.config.ClientConfig;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.protocol.NettyChannel;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Close idle connections according to core {@link ClientConfig#spareCloseTimes} to reduce resource waste.
 */
public class IdleEventListener implements EventListener<IdleEvent> {

    private static final Logger logger = LoggerFactory.getLogger(IdleEventListener.class);

    @Override
    public void onEvent(IdleEvent event) {
        NettyChannel channel = event.source();
        URL url = channel.url();
        AtomicInteger readIdleRecord = channel.get(KeyConstant.READER_IDLE_TIMES);
        AtomicInteger writeIdleRecord = channel.get(KeyConstant.WRITE_IDLE_TIMES);
        AtomicInteger allIdlerRecord = channel.get(KeyConstant.ALL_IDLE_TIMES);
        int spareCloseTimes = url.getIntParam(KeyConstant.SPARE_CLOSE_TIMES, Constant.DEFAULT_SPARE_CLOSE_TIMES);
        String enablePrintLog = System.getProperty(SystemKey.PRINT_HEARTBEAT_LOG);
        if (!StringUtil.isBlank(enablePrintLog)
                && enablePrintLog.equalsIgnoreCase(Boolean.TRUE.toString())) {
            logger.debug("channel:{},readIdleRecord:{},writeIdleRecord:{},allIdlerRecord:{}",
                    channel, readIdleRecord, writeIdleRecord, allIdlerRecord);
        }
        if (allIdlerRecord != null) {
            int allIdleTimes = allIdlerRecord.get();
            if (readIdleRecord != null) {
                int readIdleTimes = readIdleRecord.get();
                if (readIdleTimes > spareCloseTimes && allIdleTimes > spareCloseTimes) {
                    channel.close();
                }
            }
            if (writeIdleRecord != null) {
                int writeIdleTimes = writeIdleRecord.get();
                if (writeIdleTimes > spareCloseTimes && allIdleTimes > spareCloseTimes) {
                    channel.close();
                }
            }
        }
    }

}
