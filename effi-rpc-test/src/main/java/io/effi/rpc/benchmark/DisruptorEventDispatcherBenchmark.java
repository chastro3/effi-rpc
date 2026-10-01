package io.effi.rpc.benchmark;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
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

/**
 * Measures the publish path of a raw single-consumer Disruptor ring buffer.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 2)
@Fork(1)
@Threads(4)
@State(Scope.Benchmark)
public class DisruptorEventDispatcherBenchmark {

    private Disruptor<EventHolder> disruptor;

    private RingBuffer<EventHolder> ringBuffer;

    @Setup(Level.Trial)
    public void setup() {
        disruptor = new Disruptor<>(
                EventHolder::new,
                16_384,
                runnable -> {
                    Thread thread = new Thread(runnable, "disruptor-event-handler");
                    thread.setDaemon(true);
                    return thread;
                }
        );
        disruptor.handleEventsWith((holder, sequence, endOfBatch) -> {
        });
        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
    }

    @TearDown(Level.Trial)
    public void tearDown() {
        disruptor.shutdown();
    }

    @Benchmark
    public void publish(EventState state) {
        ringBuffer.publishEvent((holder, sequence) -> holder.set(state.event));
    }

    @State(Scope.Thread)
    public static class EventState {

        private final PayloadEvent event = new PayloadEvent();
    }

    private static final class PayloadEvent {
    }

    private static final class EventHolder {

        private PayloadEvent event;

        private void set(PayloadEvent event) {
            this.event = event;
        }
    }

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(DisruptorEventDispatcherBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}
