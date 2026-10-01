package io.effi.rpc.benchmark;

import io.effi.rpc.component.event.Event;
import io.effi.rpc.component.event.EventOptions;
import io.effi.rpc.component.event.MpscEventBus;
import io.effi.rpc.component.event.PublishResult;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.metrics.DefaultMetrics;
import io.effi.rpc.metrics.MetricsOptions;
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
public class MpscEventBusShardedBenchmark {

    private ScopedPlatform platform;

    private MpscEventBus bus;

    private DefaultMetrics metrics;

    @Setup(Level.Trial)
    public void setup() {
        platform = new ScopedPlatform("mpsc-event-bus-sharded-benchmark-" + System.nanoTime());
        platform.options()
                .addOption(MetricsOptions.ENABLED, false)
                .addOption(EventOptions.TELEMETRY_CONSUMERS, 4);
        metrics = new DefaultMetrics(platform);
        bus = new MpscEventBus(platform);
        metrics.register(bus.metrics());
        bus.register(PayloadEvent.class, event -> {
        });
        bus.start();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        bus.close();
        metrics.close();
        platform.close();
    }

    @Benchmark
    public PublishResult publish(EventState state) {
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
                .include(MpscEventBusShardedBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
