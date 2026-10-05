package io.effi.rpc.benchmark;

import io.effi.rpc.proxy.InvocationHandler;
import io.effi.rpc.proxy.ProxyFactory;
import io.effi.rpc.proxy.bytebuddy.ByteBuddyProxyFactory;
import io.effi.rpc.proxy.cglib.CGLibProxyFactory;
import io.effi.rpc.proxy.jdk.JDKProxyFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.CompilerControl;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Measures method dispatch overhead for the supported proxy dialects.
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@Threads(1)
@State(Scope.Benchmark)
public class ProxyInvocationBenchmark {

    private static final int VALUE = 1;

    private EchoServiceImpl impl;

    private EchoWorker worker;

    private EchoService jdk;

    private EchoService cglib;

    private EchoService bytebuddy;

    private EchoService jdkTarget;

    private EchoService cglibTarget;

    private EchoService bytebuddyTarget;

    @Setup(Level.Trial)
    public void setup() {
        impl = new EchoServiceImpl();
        worker = new EchoWorker();
        InvocationHandler dispatchHandler = (proxy, method, args, superInvoker) -> worker.invoke(args);
        InvocationHandler targetHandler = (proxy, method, args, superInvoker) -> superInvoker.invoke();
        jdk = new JDKProxyFactory().createProxy(EchoService.class, dispatchHandler);
        cglib = new CGLibProxyFactory().createProxy(EchoService.class, dispatchHandler);
        bytebuddy = new ByteBuddyProxyFactory().createProxy(EchoService.class, dispatchHandler);
        jdkTarget = new JDKProxyFactory().createProxy(impl, targetHandler);
        cglibTarget = new CGLibProxyFactory().createProxy(impl, targetHandler);
        bytebuddyTarget = new ByteBuddyProxyFactory().createProxy(impl, targetHandler);
    }

    @Benchmark
    public int direct(Input input) {
        return impl.echo(input.value);
    }

    @Benchmark
    public int jdkInterface(Input input) {
        return jdk.echo(input.value);
    }

    @Benchmark
    public int cglibInterface(Input input) {
        return cglib.echo(input.value);
    }

    @Benchmark
    public int bytebuddyInterface(Input input) {
        return bytebuddy.echo(input.value);
    }

    @Benchmark
    public int jdkTarget(Input input) {
        return jdkTarget.echo(input.value);
    }

    @Benchmark
    public int cglibTarget(Input input) {
        return cglibTarget.echo(input.value);
    }

    @Benchmark
    public int bytebuddyTarget(Input input) {
        return bytebuddyTarget.echo(input.value);
    }

    @State(Scope.Thread)
    public static class Input {

        private volatile int value = VALUE;
    }

    public static class EchoWorker {

        @CompilerControl(CompilerControl.Mode.DONT_INLINE)
        public int invoke(Object[] args) {
            return (int) args[0] + 1;
        }
    }

    public interface EchoService {

        int echo(int value);
    }

    public static class EchoServiceImpl implements EchoService {

        @Override
        public int echo(int value) {
            return value + 1;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(ProxyInvocationBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
