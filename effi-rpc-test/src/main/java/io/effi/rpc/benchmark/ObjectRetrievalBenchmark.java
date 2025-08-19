package io.effi.rpc.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 3)
@Fork(1)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class ObjectRetrievalBenchmark {

    private final SampleObject directObject = new SampleObject();

    private final Map<Class<?>, Object> classMap = new HashMap<>();
    private final Map<String, Object> stringMap = new HashMap<>();

    private final Object[] array = new Object[]{directObject};

    @Setup
    public void setup() {
        classMap.put(SampleObject.class, directObject);
        stringMap.put("sample", directObject);
    }

    @Benchmark
    public SampleObject directCall() {
        return directObject;
    }

    @Benchmark
    public SampleObject classKeyLookup() {
        return (SampleObject) classMap.get(SampleObject.class);
    }

    @Benchmark
    public SampleObject stringKeyLookup() {
        return (SampleObject) stringMap.get("sample");
    }

    @Benchmark
    public SampleObject arrayLookup() {
        return (SampleObject) array[0];
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ObjectRetrievalBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    // 一个简单的示例对象
    static class SampleObject {
        int value = 42;
    }
}
