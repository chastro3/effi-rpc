package io.effi.rpc.event;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MpmcRingBuffer<T> {
    private static final VarHandle ARRAY_HANDLE;
    private final Object[] buffer;
    private final int capacity;
    private final int mask;

    private final PaddedLong tail = new PaddedLong(0);
    private final PaddedLong cursor = new PaddedLong(0);
    private final List<Sequence> gatingSequences = new CopyOnWriteArrayList<>();

    static {
        try {
            ARRAY_HANDLE = MethodHandles.arrayElementVarHandle(Object[].class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public MpmcRingBuffer(int capacity) {
        if (Integer.bitCount(capacity) != 1) {
            throw new IllegalArgumentException("Capacity must be a power of 2");
        }
        this.capacity = capacity;
        this.mask = capacity - 1;
        this.buffer = new Object[capacity];
    }

    public void addGatingSequence(Sequence seq) {
        gatingSequences.add(seq);
    }

    public boolean offer(T item) {
        long next;
        long wrapPoint;
        long minGatingSequence;
        do {
            next = tail.get();
            wrapPoint = next - capacity;

            minGatingSequence = getMinimumGatingSequence();
            if (wrapPoint > minGatingSequence) {
                return false; // full
            }
        } while (!tail.compareAndSet(next, next + 1));

        int index = (int) (next & mask);
        ARRAY_HANDLE.setRelease(buffer, index, item);
        return true;
    }

    @SuppressWarnings("unchecked")
    public T poll(Sequence seq) {
        long current = seq.get();
        long next = current + 1;

        if (next > cursor.get()) {
            return null; // not yet available
        }

        int index = (int) (next & mask);
        T item = (T) ARRAY_HANDLE.getAcquire(buffer, index);
        if (item == null) return null; // defensive

        ARRAY_HANDLE.setRelease(buffer, index, null);
        seq.set(next);
        cursor.set(next);
        return item;
    }

    private long getMinimumGatingSequence() {
        long min = Long.MAX_VALUE;
        for (Sequence seq : gatingSequences) {
            long v = seq.get();
            if (v < min) min = v;
        }
        return min;
    }

    public static class Sequence {
        private volatile long value;
        private static final VarHandle VALUE;

        static {
            try {
                VALUE = MethodHandles.lookup().findVarHandle(Sequence.class, "value", long.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public Sequence(long initial) {
            this.value = initial;
        }

        public long get() {
            return value;
        }

        public void set(long v) {
            VALUE.setRelease(this, v);
        }
    }

    static final class PaddedLong {
        private long p1, p2, p3, p4, p5, p6, p7;
        private volatile long value;
        private long p9, p10, p11, p12, p13, p14, p15;

        private static final VarHandle VALUE;
        static {
            try {
                VALUE = MethodHandles.lookup().findVarHandle(PaddedLong.class, "value", long.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public PaddedLong(long value) {
            this.value = value;
        }

        public long get() {
            return value;
        }

        public boolean compareAndSet(long expect, long update) {
            return VALUE.compareAndSet(this, expect, update);
        }

        public void set(long value) {
            VALUE.setRelease(this, value);
        }
    }
}
