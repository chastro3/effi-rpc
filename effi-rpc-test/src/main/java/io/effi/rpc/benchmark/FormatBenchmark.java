package io.effi.rpc.benchmark;

import io.effi.rpc.util.StringUtil;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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

@BenchmarkMode(Mode.AverageTime)  // 测量每次操作的平均时间
@OutputTimeUnit(TimeUnit.NANOSECONDS)  // 输出结果为纳秒
@Fork(value = 1)  // 仅进行一次fork，并设置 JVM 堆大小
@Warmup(iterations = 3, time = 1)  // 只进行1轮热身
@Measurement(iterations = 5, time = 3)  // 测量5轮，每轮1秒
@State(Scope.Thread)
public class FormatBenchmark {

    // 测试 String.format 的性能
    @Benchmark
    public String testStringFormat() {
        return String.format("%s[local=%s, remote=%s, active=%b, type=%s]",
                "MyClass", "127.0.0.1", "192.168.1.1", true, "physical");
    }

    // 测试自定义 StringUtil.format 的性能
    @Benchmark
    public String testCustomFormat() {
        return StringUtil.format(
                "%s[local=%s, remote=%s, active=%b, type=%s]",
                "MyClass", "127.0.0.1", "192.168.1.1", true, "physical");
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(FormatBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
