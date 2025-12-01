package io.effi.rpc.component.event.v2;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.ObjectUtil;
import org.jctools.queues.MpscArrayQueue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class MpscEventDispatcher extends ScopedPlatform.Holder implements EventDispatcher {

    private static final LostEventHandler DEFAULT_LOST_EVENT_HANDLER = (event, dispatcher) -> dispatcher.dispatch(event);

    private static final Logger logger = LoggerFactory.getLogger(MpscEventDispatcher.class);

    private final MpscArrayQueue<Event> queue;

    private final int batchSize;

    private final Map<Class<? extends Event>, EventHandler<? extends Event>> handlers = new ConcurrentHashMap<>();

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final LostEventHandler lostEventHandler;

    private final Thread consumerThread;

    public MpscEventDispatcher(ScopedPlatform platform, int capacity, int batchSize) {
        this(platform, capacity, batchSize, DEFAULT_LOST_EVENT_HANDLER);
    }

    public MpscEventDispatcher(ScopedPlatform platform, int capacity, int batchSize, LostEventHandler lostEventHandler) {
        super(platform);
        this.queue = new MpscArrayQueue<>(capacity);
        this.batchSize = batchSize;
        this.lostEventHandler = lostEventHandler;
        this.consumerThread = new Thread(this::consumeLoop, "mpsc-event-dispatcher");
        start();
    }

    @Override
    public <E extends Event> void registerHandler(Class<E> eventType, EventHandler<E> handler) {
        handlers.put(eventType, handler);
    }

    @Override
    public <E extends Event> void removeHandler(Class<E> eventType) {
        handlers.remove(eventType);
    }

    @Override
    public void publish(Event event) {
        boolean ok = queue.offer(event);
        if (!ok) {
            if (lostEventHandler != null) {
                try {
                    lostEventHandler.handle(event, this);
                } catch (Exception e) {
                    logger.error("Failed to handle lost event '{}'", e, event);
                }
            }
        }
    }

    @Override
    public void start() {
        if (running.compareAndSet(false, true)) {
            consumerThread.start();
        }
    }

    @Override
    public boolean active() {
        return running.get();
    }

    @Override
    public void close() {
        if (running.compareAndSet(true, false)) {
            consumerThread.interrupt();
        }
    }

    @Override
    public int pendingCount() {
        return queue.size();
    }

    private void consumeLoop() {
        Event[] batch = new Event[batchSize];
        while (running.get()) {
            int count = 0;
            for (; count < batchSize; count++) {
                Event e = queue.poll();
                if (e == null) break;
                batch[count] = e;
            }

            if (count == 0) {
                if (Thread.interrupted()) break;
                Thread.onSpinWait();
                continue;
            }

            for (int i = 0; i < count; i++) {
                if (batch[i] != null) dispatch(batch[i]);
                batch[i] = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Event> void dispatch(E event) {
        EventHandler<E> handler = (EventHandler<E>) handlers.get(event.getClass());
        if (handler != null) {
            try {
                handler.onEvent(event);
            } catch (Exception e) {
                logger.error("Failed to handle '{}' event in '{}': {}", e,
                        ObjectUtil.simpleClassName(event), ObjectUtil.simpleClassName(handler));
            }
        }
    }

    public interface LostEventHandler {
        void handle(Event event, MpscEventDispatcher dispatcher);
    }

    public static class BlockingLostEventHandler implements LostEventHandler {
        @Override
        public void handle(Event event, MpscEventDispatcher dispatcher) {
            while (!dispatcher.queue.offer(event)) {
                Thread.onSpinWait();
            }
        }
    }
}
