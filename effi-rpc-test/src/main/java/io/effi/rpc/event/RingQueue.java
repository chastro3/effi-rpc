package io.effi.rpc.event;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.locks.LockSupport;

public class RingQueue<T> {
    private static final VarHandle ARRAY_HANDLE;

    private final Object[] buffer;
    private final int capacity;
    private final int mask;

    private final PaddedLong head = new PaddedLong(0);
    private final PaddedLong tail = new PaddedLong(0);

    private static final int SPIN_LIMIT = 100; // 自旋上限次数

    static {
        try {
            ARRAY_HANDLE = MethodHandles.arrayElementVarHandle(Object[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public RingQueue(int capacity) {
        if (Integer.bitCount(capacity) != 1) {
            throw new IllegalArgumentException("capacity must be power of 2");
        }
        this.capacity = capacity;
        this.mask = capacity - 1;
        // buffer 扩容 + 填充防止伪共享（简单示意，每个槽位之间留空）
        this.buffer = new Object[capacity * 2];
    }

    public boolean offer(T item) {
        if (item == null) throw new NullPointerException("item");

        long currentTail;
        long currentHead;
        int spin = 0;

        while (true) {
            currentTail = tail.get();
            currentHead = head.get();

            if (currentTail - currentHead >= capacity) {
                // 队列满，短暂自旋后失败
                if (++spin > SPIN_LIMIT) return false;
                LockSupport.parkNanos(1);
                continue;
            }

            if (tail.compareAndSet(currentTail, currentTail + 1)) {
                int index = (int) ((currentTail & mask) * 2);
                ARRAY_HANDLE.setRelease(buffer, index, item);
                return true;
            } else {
                // CAS失败，短暂自旋
                if (++spin > SPIN_LIMIT) LockSupport.parkNanos(1);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public T poll() {
        long currentHead;
        long currentTail;
        int spin = 0;

        while (true) {
            currentHead = head.get();
            currentTail = tail.get();

            if (currentHead >= currentTail) {
                // 队列空，短暂自旋后返回null
                if (++spin > SPIN_LIMIT) return null;
                LockSupport.parkNanos(1);
                continue;
            }

            if (head.compareAndSet(currentHead, currentHead + 1)) {
                int index = (int) ((currentHead & mask) * 2);
                T item = (T) ARRAY_HANDLE.getAcquire(buffer, index);
                ARRAY_HANDLE.setRelease(buffer, index, null);
                return item;
            } else {
                // CAS失败，短暂自旋
                if (++spin > SPIN_LIMIT) LockSupport.parkNanos(1);
            }
        }
    }

    public int size() {
        return (int) (tail.get() - head.get());
    }

    public boolean isEmpty() {
        return size() == 0;
    }
}

