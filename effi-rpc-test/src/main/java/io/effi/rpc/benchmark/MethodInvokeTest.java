package io.effi.rpc.benchmark;

import io.effi.rpc.compile.DynamicAccessor;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 3)
@Fork(1)
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class MethodInvokeTest {


    public static class Target {
        public Object hello(String name) {
            return "hello" + name;
        }
    }

    private Target target;
    private Method reflectMethod;
    private MethodHandle methodHandle;
    private DynamicAccessor dynamicAccessor;
    private int methodIndex;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        target = new Target();

        reflectMethod = Target.class.getMethod("hello", String.class);

        methodHandle = MethodHandles.lookup()
                .findVirtual(Target.class, "hello", MethodType.methodType(Object.class, String.class));

        dynamicAccessor = DynamicAccessor.get(Target.class);
        methodIndex = dynamicAccessor.getMethodIndex("hello", String.class);

    }

    @Benchmark
    public Object directCall() {
        return target.hello("zzz");
    }

    @Benchmark
    public Object reflectCall() throws Exception {
        return reflectMethod.invoke(target, "zzz");
    }

    @Benchmark
    public Object methodHandleCall() throws Throwable {
        return methodHandle.invokeExact(target, "zzz");
    }

    @Benchmark
    public Object dynamicAccessorCall() {
        return dynamicAccessor.invoke(target, methodIndex, "zzz");
    }


    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(MethodInvokeTest.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

}
