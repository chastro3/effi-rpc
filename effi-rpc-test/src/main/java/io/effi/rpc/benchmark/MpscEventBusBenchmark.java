package io.effi.rpc.benchmark;

import io.effi.rpc.component.event.Event;
import io.effi.rpc.component.event.MpscEventBus;
import io.effi.rpc.component.event.PublishResult;
import io.effi.rpc.component.ScopedPlatform;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(1)
@Threads(4)
@State(Scope.Benchmark)
public class MpscEventBusBenchmark {

    private ScopedPlatform platform;

    private MpscEventBus bus;

    @Setup(Level.Trial)
    public void setup() {
        platform = new ScopedPlatform("mpsc-event-bus-benchmark-" + System.nanoTime());
        bus = new MpscEventBus(platform);
        bus.register(PayloadEvent.class, event -> {
        });
        bus.start();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        bus.close();
        platform.close();
    }

    @Benchmark
    public PublishResult publishMpsc(EventState state) {
        return bus.publish(state.event);
    }

    @State(Scope.Thread)
    public static class EventState {

        private final PayloadEvent event = new PayloadEvent();
    }

    private static final class PayloadEvent implements Event {
    }


    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(MpscEventBusBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
