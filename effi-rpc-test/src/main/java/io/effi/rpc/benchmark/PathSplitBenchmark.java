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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 3)
@Fork(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class PathSplitBenchmark {

    private static final String TEST_PATH = "/a/b//c///d/e/f///g/h/i/j";


    @Benchmark
    public String[] a(){
        return methodA(TEST_PATH);
    }

    @Benchmark
     public String[] b(){
        return methodB(TEST_PATH);
    }

     @Benchmark
      public String[] c(){
        return methodC(TEST_PATH);
    }


    // 方法A：List收集再toArray
    public static String[] methodA(String path) {
        if (StringUtil.isBlank(path)) {
            return StringUtil.emptyArray();
        }
        String[] parts = path.split("/");
        List<String> list = new ArrayList<>();
        for (String part : parts) {
            if (StringUtil.isBlank(path)) {
                list.add(part);
            }
        }
        return list.toArray(StringUtil.emptyArray());
    }

    // 方法B：双遍历计数+一次性数组填充
    public static String[] methodB(String path) {
        if (StringUtil.isBlank(path)) {
            return StringUtil.emptyArray();
        }
        String[] parts = path.split("/");
        int count = 0;
        for (String part : parts) {
            if (!part.isEmpty()) {
                count++;
            }
        }
        String[] result = new String[count];
        int index = 0;
        for (String part : parts) {
            if (!part.isEmpty()) {
                result[index++] = part;
            }
        }
        return result;
    }

    public static String[] methodC(String path) {
        if (StringUtil.isBlank(path)) {
            return StringUtil.emptyArray();
        }
        String[] parts = path.split("/");
        String[] temp = new String[parts.length];
        int count = 0;
        for (String part : parts) {
            if (StringUtil.isNotBlank(part)) {
                temp[count++] = part;
            }
        }
        if (count == 0) {
            return StringUtil.emptyArray();
        }
        // 截断数组返回
        return Arrays.copyOf(temp, count);
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(PathSplitBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
