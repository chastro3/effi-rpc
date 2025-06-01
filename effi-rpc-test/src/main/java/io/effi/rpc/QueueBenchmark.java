package io.effi.rpc;

import io.effi.rpc.event.RingQueue;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class QueueBenchmark {
    // 你的自定义环形队列接口（示例）
    interface SimpleQueue<E> {
        boolean offer(E e);

        E poll();
    }


    // 示例：自定义 RingQueue 实现（简化版）
    static class RingQueueWrapper<E> implements SimpleQueue<E> {
        private RingQueue<E> queue;

        public RingQueueWrapper(RingQueue<E> queue) {
            this.queue = queue;
        }

        public boolean offer(E e) {
            return queue.offer(e);
        }

        public E poll() {
            return queue.poll();
        }
    }

    // 包装 JDK 队列适配 SimpleQueue 接口
    static class JdkQueueWrapper<E> implements SimpleQueue<E> {
        private final Queue<E> queue;

        public JdkQueueWrapper(Queue<E> queue) {
            this.queue = queue;
        }

        public boolean offer(E e) {
            return queue.offer(e);
        }

        public E poll() {
            return queue.poll();
        }
    }

    // 基准测试方法
    static void benchmark(SimpleQueue<Integer> queue, int producers, int consumers, int seconds, String name) throws InterruptedException {
        AtomicLong produced = new AtomicLong();
        AtomicLong consumed = new AtomicLong();

        ExecutorService executor = Executors.newFixedThreadPool(producers + consumers);

        Runnable producer = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                if (queue.offer(1)) {
                    produced.incrementAndGet();
                } else {
                    Thread.yield();
                }
            }
        };

        Runnable consumer = () -> {
            while (!Thread.currentThread().isInterrupted()) {
                Integer item = queue.poll();
                if (item != null) {
                    consumed.incrementAndGet();
                } else {
                    Thread.yield();
                }
            }
        };

        for (int i = 0; i < producers; i++) executor.submit(producer);
        for (int i = 0; i < consumers; i++) executor.submit(consumer);

        Thread.sleep(seconds * 1000);

        executor.shutdownNow();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        System.out.printf("%s - Produced: %d, Consumed: %d, Throughput: %d ops/sec%n",
                name, produced.get(), consumed.get(), consumed.get() / seconds);
    }

    public static void main(String[] args) throws InterruptedException {
        int capacity = 1024 * 1024;
        int producers = 4;
        int consumers = 4;
        int testDuration = 10;

        // 测试自定义 RingQueue
        SimpleQueue<Integer> ringQueue = new RingQueueWrapper<>(new RingQueue<>(capacity));
        benchmark(ringQueue, producers, consumers, testDuration, "RingQueue");

        // 测试 JDK ConcurrentLinkedQueue
        SimpleQueue<Integer> clq = new JdkQueueWrapper<>(new ConcurrentLinkedQueue<>());
        benchmark(clq, producers, consumers, testDuration, "ConcurrentLinkedQueue");

        // 测试 JDK ArrayBlockingQueue
        SimpleQueue<Integer> abq = new JdkQueueWrapper<>(new ArrayBlockingQueue<>(capacity));
        benchmark(abq, producers, consumers, testDuration, "ArrayBlockingQueue");
    }
}
