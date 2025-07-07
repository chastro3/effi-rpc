package demo.consumer;

import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * @Author WenBo Zhou
 * @Date 2025/6/5 16:47
 */
public class ScheduleMain {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleMain.class);

    public static void main(String[] args) {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(5);
        service.scheduleAtFixedRate(() -> {
            logger.info("哈哈哈");
        }, 1000, 1000, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
}
