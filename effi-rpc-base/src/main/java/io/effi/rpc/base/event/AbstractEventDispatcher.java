package io.effi.rpc.base.event;

import io.effi.rpc.component.EffiRpcPlatform;
import io.effi.rpc.component.PlatformSource;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.ObjectUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an abstract implementation of {@link EventDispatcher}.
 */
public abstract class AbstractEventDispatcher extends PlatformSource.Holder implements EventDispatcher {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Map<Class<? extends Event<?>>, List<EventListener<?>>> listenerMap = new ConcurrentHashMap<>();

    protected AbstractEventDispatcher(EffiRpcPlatform platform) {
        super(platform);
    }

    @Override
    public <E extends Event<?>> void registerListener(Class<E> eventType, EventListener<E> listener) {
        listenerMap.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
        logger.debug("Registered {}<{}>", ObjectUtil.simpleClassName(listener), ObjectUtil.simpleClassName(eventType));
    }

    @Override
    public <E extends Event<?>> void removeListener(Class<E> eventType, EventListener<E> listener) {
        if (listenerMap.containsKey(eventType)) {
            List<EventListener<?>> listeners = listenerMap.get(eventType);
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listenerMap.remove(eventType);
            }
            logger.debug("Removed {}<{}>", ObjectUtil.simpleClassName(listener), ObjectUtil.simpleClassName(eventType));
        }
    }

    @Override
    public <E extends Event<?>> void publish(E event) {
        if (event != null && event.allowPropagation()) {
            doPublish(event);
            logger.trace("Publish <{}>", ObjectUtil.simpleClassName(event));
        }
    }

    protected abstract <E extends Event<?>> void doPublish(E event);

}
