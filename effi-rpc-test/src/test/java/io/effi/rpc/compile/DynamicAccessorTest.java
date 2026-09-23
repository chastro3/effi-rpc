package io.effi.rpc.compile;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DynamicAccessorTest {

    public interface Sample {

        String echo(String value);

        int add(int left, int right);

        void noop();

        static String staticEcho(String value) {
            return "static:" + value;
        }
    }

    public static class NestedTarget {

        public String value() {
            return "nested";
        }
    }

    public interface OverloadedSample {

        String value(String value);

        String value(String value, int count);
    }

    public interface BaseSample {

        default String defaultValue() {
            return "default";
        }
    }

    public interface InheritedSample extends BaseSample {

        String own();
    }

    public interface ThrowingSample {

        String checked() throws IOException;
    }

    @Test
    void cachesAccessorPerType() {
        assertSame(DynamicAccessor.fetch(Sample.class), DynamicAccessor.fetch(Sample.class));
    }

    @Test
    void invokesInterfaceMethods() {
        DynamicAccessor accessor = DynamicAccessor.fetch(Sample.class);
        Sample sample = new Sample() {
            @Override
            public String echo(String value) {
                return value;
            }

            @Override
            public int add(int left, int right) {
                return left + right;
            }

            @Override
            public void noop() {
            }
        };

        int echoIndex = accessor.findMethodIndex("echo", String.class);
        int addIndex = accessor.findMethodIndex("add", int.class, int.class);
        int noopIndex = accessor.findMethodIndex("noop");

        assertEquals("value", accessor.invoke(sample, echoIndex, "value"));
        assertEquals(3, accessor.invoke(sample, addIndex, 1, 2));
        assertNull(accessor.invoke(sample, noopIndex));
    }

    @Test
    void invokesNestedTarget() {
        DynamicAccessor accessor = DynamicAccessor.fetch(NestedTarget.class);
        int valueIndex = accessor.findMethodIndex("value");

        assertEquals("nested", accessor.invoke(new NestedTarget(), valueIndex));
    }

    @Test
    void invokesStaticMethodWithoutTarget() {
        DynamicAccessor accessor = DynamicAccessor.fetch(Sample.class);
        int staticEchoIndex = accessor.findMethodIndex("staticEcho", String.class);

        assertEquals("static:value", accessor.invoke(null, staticEchoIndex, "value"));
    }

    @Test
    void validatesGeneratedAccessorArguments() {
        DynamicAccessor accessor = DynamicAccessor.fetch(Sample.class);
        Sample sample = new Sample() {
            @Override
            public String echo(String value) {
                return value;
            }

            @Override
            public int add(int left, int right) {
                return left + right;
            }

            @Override
            public void noop() {
            }
        };
        int addIndex = accessor.findMethodIndex("add", int.class, int.class);

        assertThrows(IllegalArgumentException.class, () -> accessor.invoke(sample, addIndex, 1));
        assertThrows(IllegalArgumentException.class, () -> accessor.invoke(sample, addIndex, 1, null));
        assertThrows(IllegalArgumentException.class, () -> accessor.invoke(sample, 999, 1, 2));
    }

    @Test
    void methodHandleFallbackInvokesMethods() {
        DynamicAccessor accessor = MethodHandleDynamicAccessor.create(Sample.class);
        Sample sample = new Sample() {
            @Override
            public String echo(String value) {
                return value;
            }

            @Override
            public int add(int left, int right) {
                return left + right;
            }

            @Override
            public void noop() {
            }
        };

        int echoIndex = accessor.findMethodIndex("echo", String.class);
        int addIndex = accessor.findMethodIndex("add", int.class, int.class);
        int noopIndex = accessor.findMethodIndex("noop");
        int staticEchoIndex = accessor.findMethodIndex("staticEcho", String.class);

        assertEquals("value", accessor.invoke(sample, echoIndex, "value"));
        assertEquals(3, accessor.invoke(sample, addIndex, 1, 2));
        assertNull(accessor.invoke(sample, noopIndex));
        assertEquals("static:value", accessor.invoke(null, staticEchoIndex, "value"));
    }

    @Test
    void reflectiveFallbackInvokesNestedTarget() {
        DynamicAccessor accessor = ReflectiveDynamicAccessor.create(NestedTarget.class);
        int valueIndex = accessor.findMethodIndex("value");

        assertEquals("nested", accessor.invoke(new NestedTarget(), valueIndex));
    }

    @Test
    void resolvesOverloadedMethods() {
        DynamicAccessor accessor = DynamicAccessor.fetch(OverloadedSample.class);
        OverloadedSample sample = new OverloadedSample() {
            @Override
            public String value(String value) {
                return value;
            }

            @Override
            public String value(String value, int count) {
                return value.repeat(count);
            }
        };

        int singleIndex = accessor.findMethodIndex("value", String.class);
        int repeatedIndex = accessor.findMethodIndex("value", String.class, int.class);

        assertEquals("a", accessor.invoke(sample, singleIndex, "a"));
        assertEquals("aaa", accessor.invoke(sample, repeatedIndex, "a", 3));
    }

    @Test
    void invokesInheritedDefaultMethod() {
        DynamicAccessor accessor = DynamicAccessor.fetch(InheritedSample.class);
        InheritedSample sample = () -> "own";

        assertEquals("default", accessor.invoke(sample, accessor.findMethodIndex("defaultValue")));
        assertEquals("own", accessor.invoke(sample, accessor.findMethodIndex("own")));
    }

    @Test
    void propagatesTargetExceptions() {
        DynamicAccessor accessor = DynamicAccessor.fetch(ThrowingSample.class);
        IOException failure = new IOException("boom");
        ThrowingSample sample = () -> {
            throw failure;
        };

        IOException thrown = assertThrows(
                IOException.class,
                () -> accessor.invoke(sample, accessor.findMethodIndex("checked"))
        );
        assertSame(failure, thrown);
    }

    @Test
    void isolatesAccessorByTargetClassLoader() throws Exception {
        ClassLoader isolatedLoader = new ChildFirstClassLoader(Sample.class);
        Class<?> isolatedType = isolatedLoader.loadClass(Sample.class.getName());
        Object proxy = Proxy.newProxyInstance(
                isolatedLoader,
                new Class<?>[]{isolatedType},
                (instance, method, args) -> {
                    if (method.getName().equals("echo")) {
                        return args[0];
                    }
                    if (method.getName().equals("add")) {
                        return (Integer) args[0] + (Integer) args[1];
                    }
                    return null;
                }
        );
        DynamicAccessor accessor = DynamicAccessor.fetch(isolatedType);
        int echoIndex = accessor.findMethodIndex("echo", String.class);

        assertSame(isolatedType, proxy.getClass().getInterfaces()[0]);
        assertEquals("isolated", accessor.invoke(proxy, echoIndex, "isolated"));
    }

    @Test
    void concurrentFetchReturnsSameAccessor() throws Exception {
        int threads = 16;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<DynamicAccessor>> futures = new ArrayList<>(threads);
        try {
            for (int i = 0; i < threads; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    return DynamicAccessor.fetch(Sample.class);
                }));
            }
            start.countDown();
            DynamicAccessor expected = futures.get(0).get();
            for (Future<DynamicAccessor> future : futures) {
                assertSame(expected, future.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static final class ChildFirstClassLoader extends ClassLoader {

        private ChildFirstClassLoader(Class<?> type) {
            super(type.getClassLoader());
            this.type = type;
        }

        private final Class<?> type;

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.equals(type.getName())) {
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) {
                        try (InputStream input = type.getResourceAsStream(resourceName(type))) {
                            byte[] bytes = input.readAllBytes();
                            loaded = defineClass(name, bytes, 0, bytes.length);
                        } catch (IOException e) {
                            throw new ClassNotFoundException(name, e);
                        }
                    }
                    if (resolve) {
                        resolveClass(loaded);
                    }
                    return loaded;
                }
            }
            return super.loadClass(name, resolve);
        }

        private static String resourceName(Class<?> type) {
            return "/" + type.getName().replace('.', '/') + ".class";
        }
    }
}
