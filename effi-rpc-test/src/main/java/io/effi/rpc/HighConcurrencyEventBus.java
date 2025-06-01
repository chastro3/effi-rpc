package io.effi.rpc;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Consumer;

public class HighConcurrencyEventBus<T> {

    // 每个消费者线程绑定一个环形队列
    static class RingQueue<E> {
        private final AtomicReferenceArray<E> buffer;
        private final int capacity;

        // 使用填充结构避免伪共享
        static class PaddedAtomicInteger extends AtomicInteger {
            public long p1, p2, p3, p4, p5, p6, p7;

            public PaddedAtomicInteger(int initialValue) {
                super(initialValue);
            }
        }

        private final PaddedAtomicInteger head = new PaddedAtomicInteger(0);
        private final PaddedAtomicInteger tail = new PaddedAtomicInteger(0);

        // 构造及方法不变

        public RingQueue(int capacity) {
            this.capacity = capacity;
            this.buffer = new AtomicReferenceArray<>(capacity);
        }

        public boolean offer(E e) {
            int currentTail;
            int nextTail;
            do {
                currentTail = tail.get();
                nextTail = (currentTail + 1) % capacity;
                if (nextTail == head.get()) {
                    // 队列满
                    return false;
                }
            } while (!tail.compareAndSet(currentTail, nextTail));
            buffer.set(currentTail, e);
            return true;
        }

        public E poll() {
            int currentHead = head.get();
            if (currentHead == tail.get()) {
                return null; // 空
            }
            E e = buffer.get(currentHead);
            buffer.set(currentHead, null);
            head.set((currentHead + 1) % capacity);
            return e;
        }
    }

    // 订阅者组（轮询分发）
    static class SubscriberGroup<E> {
        private final Consumer<E>[] consumers;
        private final AtomicInteger idx = new AtomicInteger(0);

        @SafeVarargs
        public SubscriberGroup(Consumer<E>... consumers) {
            this.consumers = consumers;
        }

        public Consumer<E> next() {
            int i = Math.abs(idx.getAndIncrement() % consumers.length);
            return consumers[i];
        }
    }

    private final RingQueue<T>[] queues;
    private final Thread[] workers;
    private final SubscriberGroup<T> subscriberGroup;

    public HighConcurrencyEventBus(int consumerThreads, int queueSize, SubscriberGroup<T> subscribers) {
        this.subscriberGroup = subscribers;
        this.queues = new RingQueue[consumerThreads];
        this.workers = new Thread[consumerThreads];
        for (int i = 0; i < consumerThreads; i++) {
            queues[i] = new RingQueue<>(queueSize);
            final int index = i;
            workers[i] = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    T event = queues[index].poll();
                    if (event != null) {
                        try {
                            Consumer<T> consumer = subscriberGroup.next();
                            consumer.accept(event);
                        } catch (Exception e) {
                            e.printStackTrace(); // 可替换为日志
                        }
                    } else {
                        Thread.yield();
                    }
                }
            }, "Consumer-" + i);
            workers[i].start();
        }
    }

    // 简单哈希分配队列，保证事件均匀分布
    public boolean publish(T event) {
        int index = (event.hashCode() & Integer.MAX_VALUE) % queues.length;
        return queues[index].offer(event);
    }

    public void shutdown() {
        for (Thread worker : workers) {
            worker.interrupt();
        }
    }
}
