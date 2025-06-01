package io.effi.rpc.event;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class PaddedLong {
    // 前置填充（64 字节对齐）
    private long p1, p2, p3, p4, p5, p6, p7;

    // 核心值
    private volatile long value;
    long p8, p9, p10, p11, p12, p13, p14;

    private static final VarHandle VALUE;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            VALUE = l.findVarHandle(PaddedLong.class, "value", long.class);
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

    public void set(long v) {
        value = v;
    }

    public boolean compareAndSet(long expect, long update) {
        return VALUE.compareAndSet(this, expect, update);
    }

    public long getAndAdd(long delta) {
        return (long) VALUE.getAndAdd(this, delta);
    }

    public long incrementAndGet() {
        return (long) VALUE.getAndAdd(this, 1L) + 1;
    }
}


