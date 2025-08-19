package io.effi.rpc.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3)
@Measurement(iterations = 5)
@State(Scope.Thread)
public class DCLBenchmark {

    static class FieldType {
        final int value;

        FieldType(int value) {
            this.value = value;
        }
    }

    // volatile 字段
    private volatile FieldType field;

    private AtomicInteger counter = new AtomicInteger(0);

    // 1. 使用局部变量快照
    public FieldType getFieldWithLocal() {
        FieldType result = field;
        if (result != null) return result;
        synchronized (this) {
            result = field;
            return result != null ? result : (field = computeField());
        }
    }

    // 2. 直接访问 volatile
    public FieldType getFieldDirect() {
        if (field != null) return field;
        synchronized (this) {
            return field != null ? field : (field = computeField());
        }
    }

    public FieldType getFieldDcl() {
        if (field == null) {
            synchronized (this) {
                if (field == null) field = computeField();
            }
        }
        return field;
    }

    private FieldType computeField() {
        return new FieldType(counter.incrementAndGet());
    }

    // JMH 基准测试方法
    @Benchmark
    public FieldType testLocal() {
        return getFieldWithLocal();
    }

    @Benchmark
    public FieldType testDirect() {
        return getFieldDirect();
    }

    @Benchmark
    public FieldType testDcl() {
        return getFieldDcl();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(DCLBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
