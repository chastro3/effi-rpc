package io.effi.rpc.benchmark;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class ChainBenchmark {

    @Param({"5", "10", "15", "20", "50", "100"})
    public int filterCount;

    private IndexedChain<String> indexedChain;
    private FilterChain<String> recursiveChain;

    private FilterChain<String> recursiveNodeChain;

    private FilterChain<String> arrayChain;

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ChainBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }

    @Setup
    public void setup() {
        List<Filter<String>> filters = new ArrayList<>();
        for (int i = 0; i < filterCount; i++) {
            filters.add((ctx, chain) -> chain.doFilter(ctx));
        }
        indexedChain = new IndexedChain<>(filters);
        recursiveChain = RecursiveChain.init(filters);
        recursiveNodeChain = RecursiveChainNde.init(filters);
        arrayChain = new IndexedArrayChain<>(filters);
    }

    @Benchmark
    public String testIndexedChain() {
        return indexedChain.doFilter("context");
    }

    @Benchmark
    public String testRecursiveChain() {
        return recursiveChain.doFilter("context");
    }

    @Benchmark
    public String testRecursiveChainNode() {
        return recursiveNodeChain.doFilter("context");
    }

    @Benchmark
    public String testZArrayChain() {
        return arrayChain.doFilter("context");
    }

    // === Filter/Chain common interfaces ===

    public interface Filter<C> {
        String doFilter(C ctx, FilterChain<C> chain);
    }

    public interface FilterChain<C> {
        String doFilter(C ctx);
    }

    // === Indexed Chain ===

    public static class IndexedChain<C> implements FilterChain<C> {

        private final IndexedChainNode<C> head;

        public IndexedChain(List<Filter<C>> filters) {
            if (filters == null || filters.isEmpty()) {
                head = null;
            } else {
                head = buildChain(filters, 0);
            }
        }

        private IndexedChainNode<C> buildChain(List<Filter<C>> filters, int index) {
            if (index >= filters.size()) {
                return null;
            }
            return new IndexedChainNode<>(filters.get(index), buildChain(filters, index + 1));
        }

        @Override
        public String doFilter(C ctx) {
            if (head == null) return "done";
            return head.doFilter(ctx);
        }

        private record IndexedChainNode<C>(Filter<C> filter,
                                           IndexedChainNode<C> next) implements FilterChain<C> {

            @Override
            public String doFilter(C ctx) {
                if (next == null) {
                    return filter.doFilter(ctx, c -> "done");
                } else {
                    return filter.doFilter(ctx, next);
                }
            }
        }
    }

    // === Recursive Chain with pre-built next ===
    public static class RecursiveChainNde<C> implements FilterChain<C> {

        private final Node<C> head;

        private RecursiveChainNde(Node<C> head) {
            this.head = head;
        }

        public static <C> FilterChain<C> init(List<Filter<C>> filters) {
            Node<C> next = null;
            for (int i = filters.size() - 1; i >= 0; i--) {
                next = new Node<>(filters.get(i), next);
            }
            return new RecursiveChainNde<>(next);
        }

        @Override
        public String doFilter(C ctx) {
            if (head == null) {
                return "done";
            }
            return head.doFilter(ctx);
        }

        private static class Node<C> implements FilterChain<C> {
            private final Filter<C> filter;
            private final Node<C> next;

            Node(Filter<C> filter, Node<C> next) {
                this.filter = filter;
                this.next = next;
            }

            @Override
            public String doFilter(C ctx) {
                if (next == null) {
                    // 最后一个节点，传入空链
                    return filter.doFilter(ctx, c -> "done");
                }
                return filter.doFilter(ctx, next);
            }
        }
    }


    public static class RecursiveChain<C> implements FilterChain<C> {

        private final Filter<C> filter;

        private final RecursiveChain<C> next;

        private RecursiveChain(Filter<C> filter, RecursiveChain<C> next) {
            this.filter = filter;
            this.next = next;
        }

        public static <C> FilterChain<C> init(List<Filter<C>> filters) {
            RecursiveChain<C> next = null; // end
            for (int i = filters.size() - 1; i >= 0; i--) {
                next = new RecursiveChain<>(filters.get(i), next);
            }
            return next;
        }

        @Override
        public String doFilter(C ctx) {
            if (next == null) {
                return filter.doFilter(ctx, c -> "done");
            }
            return filter.doFilter(ctx, next);
        }

    }

    public static class IndexedArrayChain<C> implements FilterChain<C> {

        private final Filter<C>[] filters;

        @SuppressWarnings("unchecked")
        public IndexedArrayChain(List<Filter<C>> filterList) {
            this.filters = (Filter<C>[]) filterList.toArray(new Filter[0]);
        }

        @Override
        public String doFilter(C ctx) {
            return doFilter(ctx, 0);
        }

        private String doFilter(C ctx, int index) {
            if (index >= filters.length) return "done";
            return filters[index].doFilter(ctx, c -> doFilter(c, index + 1));
        }
    }
}

