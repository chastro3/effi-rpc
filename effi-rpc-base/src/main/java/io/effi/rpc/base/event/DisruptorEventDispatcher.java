package io.effi.rpc.base.event;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.executor.RpcThreadFactory;
import io.effi.rpc.util.ObjectUtil;

/**
 * Implements {@link EventDispatcher} using Disruptor.
 */
public class DisruptorEventDispatcher extends AbstractEventDispatcher {

    private int bufferSize;

    private int consumerSize;

    private final RingBuffer<EventHolder<?>> ringBuffer;

    private Disruptor<EventHolder<?>> disruptor;

    private volatile boolean started;

    public DisruptorEventDispatcher(EffiRpcPlatform platform) {
        super(platform);
        this.ringBuffer = buildRingBuffer();

    }

    @Override
    @SuppressWarnings("unchecked")
    protected <E extends Event<?>> void doPublish(E event) {
        ringBuffer.publishEvent((eventHolder, sequence) -> {
            EventHolder<E> holder = (EventHolder<E>) eventHolder;
            holder.set(event);
        });
    }

    @Override
    public synchronized void close() {
        if (disruptor != null) {
            disruptor.shutdown();
        }
        started = false;
    }

    @Override
    public boolean isActive() {
        return started;
    }

    private RingBuffer<EventHolder<?>> buildRingBuffer() {
        int bufferSize = Constant.DEFAULT_BUFFER_SIZE;
        int subscribes = Constant.DEFAULT_SUBSCRIBES;
        Disruptor<EventHolder<?>> disruptor = new Disruptor<>(
                EventHolder::new,
                bufferSize,
                new RpcThreadFactory("disruptor-event-handler"));
        RingBuffer<EventHolder<?>> ringBuffer = disruptor.getRingBuffer();
        DisruptorEventHandler<?>[] handlers = new DisruptorEventHandler<?>[subscribes];
        for (int i = 0; i < subscribes; i++) {
            handlers[i] = new DisruptorEventHandler<>();
        }
        disruptor.handleEventsWith(handlers);
        disruptor.setDefaultExceptionHandler(new DisruptorExceptionHandler());
        disruptor.start();
        this.started = true;
        this.disruptor = disruptor;
        return ringBuffer;
    }

    static final class EventHolder<E extends Event<?>> extends io.effi.rpc.util.Holder<E> {}

    class DisruptorExceptionHandler implements ExceptionHandler<EventHolder<?>> {

        @Override
        public void handleEventException(Throwable e, long sequence, EventHolder<?> event) {
            logger.error("{} handle <{}> failed", e, ObjectUtil.simpleClassName(this), ObjectUtil.simpleClassName(event.get()));
        }

        @Override
        public void handleOnStartException(Throwable e) {
            logger.error(ObjectUtil.simpleClassName(this) + " start started", e);
        }

        @Override
        public void handleOnShutdownException(Throwable e) {
            logger.error(ObjectUtil.simpleClassName(this) + " shutdown failed", e);
        }

    }

    class DisruptorEventHandler<E extends Event<?>> implements EventHandler<EventHolder<E>> {

        @SuppressWarnings("unchecked")
        @Override
        public void onEvent(EventHolder<E> holder, long sequence, boolean endOfBatch) throws Exception {
            E event = holder.get();
            if (event.stopPropagation()) {
                var entries = listenerMap.entrySet();
                for (var entry : entries) {
                    if (entry.getKey().isAssignableFrom(event.getClass())) {
                        for (EventListener<?> listener : entry.getValue()) {
                            try {
                                ((EventListener<E>) listener).onEvent(event);
                            } catch (Exception e) {
                                throw PredefinedErrorCode.HANDLE_EVENT.fail(e,
                                        ObjectUtil.simpleClassName(listener),
                                        ObjectUtil.simpleClassName(event)
                                );
                            } finally {
                                event.stopPropagation();
                            }
                        }
                    }
                }
            }
        }
    }
}

