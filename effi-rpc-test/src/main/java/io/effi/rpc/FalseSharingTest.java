package io.effi.rpc;

public class FalseSharingTest {

    static final int THREAD_COUNT = 2;
    static final long ITERATIONS = 1000L * 1000 * 1000;

    // 情况一：存在伪共享
    static class FalseSharingData {
        public volatile long value = 0L;
    }

    // 情况二：避免伪共享（手动填充64字节）
    static class PaddedData {
        public volatile long value = 0L;
        // 填充字段，总共占 64 字节（1 个 long 是 8 字节）
        public long p1, p2, p3, p4, p5, p6, p7;
    }

    public static void main(String[] args) throws Exception {
        runTest("伪共享测试", new FalseSharingData[THREAD_COUNT]);
        runTest("避免伪共享测试", new PaddedData[THREAD_COUNT]);
    }

    private static <T> void runTest(String label, T[] datas) throws Exception {
        // 初始化数组元素
        for (int i = 0; i < datas.length; i++) {
            datas[i] = (datas instanceof FalseSharingData[]) ? (T) new FalseSharingData() : (T) new PaddedData();
        }

        Thread[] threads = new Thread[THREAD_COUNT];
        long start = System.nanoTime();

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int idx = i;
            threads[i] = new Thread(() -> {
                for (long j = 0; j < ITERATIONS; j++) {
                    if (datas[idx] instanceof FalseSharingData) {
                        ((FalseSharingData) datas[idx]).value = j;
                    } else {
                        ((PaddedData) datas[idx]).value = j;
                    }
                }
            });
        }

        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();

        long durationMs = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("%s 耗时: %d ms%n", label, durationMs);
    }
}
