package io.effi.rpc.component.event.v2;

/**
 * 单个事件处理器
 */
@FunctionalInterface
public interface EventHandler<E extends Event> {

    /**
     * 处理单个事件
     */
    void onEvent(E event);

}
