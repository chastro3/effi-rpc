package io.effi.rpc.context;

import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.metrics.DefaultMetrics;
import io.effi.rpc.component.tools.Scheduler;
import io.effi.rpc.concurrent.Result;
import io.effi.rpc.config.SmartURL;
import io.effi.rpc.context.metrics.CallerMetrics;
import io.effi.rpc.context.support.CallExecution;
import io.effi.rpc.context.support.Unary;
import io.effi.rpc.context.invocation.PositionalInvocation;
import io.effi.rpc.exception.EffiRpcException;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.metrics.MetricKey;
import io.effi.rpc.option.OptionName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.effi.rpc.context.options.CallerOptions.TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallExecutionTest {

    @Test
    void retriesUntilSuccess() throws Exception {
        EffiRpcException firstFailure = PredefinedErrorCode.SERVICE_UNAVAILABLE.fail("first");
        Fixture fixture = new Fixture(-1L);
        fixture.failureHandler = (context, failureCount, cause) -> {
            if (failureCount > 1) {
                throw cause;
            }
        };
        fixture.attemptFactory = (context, attempt) -> {
            ReplyFuture future = new ReplyFuture(context);
            if (attempt == 1) {
                future.failure(firstFailure);
            } else {
                complete(future, context, "ok", fixture.response);
            }
            return future;
        };

        CallExecution<String> execution = fixture.newExecution();
        Result<String> result = execution.execute().await();

        assertTrue(result.succeeded());
        assertEquals("ok", result.value());
        assertEquals(2, fixture.attempts.get());
        assertEquals(2L, fixture.counter(CallerMetrics.CALL_COUNT));
        assertEquals(1L, fixture.counter(CallerMetrics.CALL_COUNT.withTag("status", "success")));
        assertEquals(1L, fixture.counter(CallerMetrics.CALL_COUNT.withTag("status", "failure")));
        assertEquals(2L, fixture.timerCount(CallerMetrics.CALL_DURATION));
    }

    @Test
    void failsAfterRetriesAreExhausted() throws Exception {
        EffiRpcException failure = PredefinedErrorCode.SERVICE_UNAVAILABLE.fail("unavailable");
        Fixture fixture = new Fixture(-1L);
        fixture.failureHandler = (context, failureCount, cause) -> {
            if (failureCount > 1) {
                throw cause;
            }
        };
        fixture.attemptFactory = (context, attempt) -> {
            ReplyFuture future = new ReplyFuture(context);
            future.failure(failure);
            return future;
        };

        CallExecution<String> execution = fixture.newExecution();
        Result<String> result = execution.execute().await();

        assertTrue(result.failed());
        assertSame(failure, result.cause());
        assertEquals(2, fixture.attempts.get());
        assertEquals(2L, fixture.counter(CallerMetrics.CALL_COUNT));
        assertEquals(0L, fixture.counter(CallerMetrics.CALL_COUNT.withTag("status", "success")));
        assertEquals(2L, fixture.counter(CallerMetrics.CALL_COUNT.withTag("status", "failure")));
    }

    @Test
    void deadlineCancelsActiveAttempt() throws Exception {
        Fixture fixture = new Fixture(30L);
        fixture.failureHandler = (context, failureCount, cause) -> {
            throw cause;
        };
        fixture.attemptFactory = (context, attempt) -> new ReplyFuture(context);

        CallExecution<String> execution = fixture.newExecution();
        Result<String> result = execution.execute().await();

        assertTrue(result.failed());
        assertEquals(PredefinedErrorCode.DEADLINE_EXCEEDED, result.cause().errorCode());
        assertTrue(fixture.lastAttempt().completed());
        assertEquals(1L, fixture.counter(CallerMetrics.TIMEOUT_COUNT));
        assertEquals(1L, fixture.counter(CallerMetrics.CALL_COUNT));
        assertEquals(1L, fixture.counter(CallerMetrics.CALL_COUNT.withTag("status", "failure")));
    }

    @Test
    void cancelStopsExecution() throws Exception {
        EffiRpcException reason = PredefinedErrorCode.CALL_CANCELLED.fail("test");
        Fixture fixture = new Fixture(-1L);
        fixture.failureHandler = (context, failureCount, cause) -> {
            throw cause;
        };
        fixture.attemptFactory = (context, attempt) -> new ReplyFuture(context);

        CallExecution<String> execution = fixture.newExecution();
        execution.execute();

        assertTrue(execution.cancel(reason));
        Result<String> result = execution.execute().await();

        assertTrue(result.failed());
        assertSame(reason, result.cause());
        assertSame(reason, fixture.lastAttempt().await().cause());
        assertEquals(0L, fixture.counter(CallerMetrics.CALL_COUNT));
    }

    @Test
    void failedStageResultIsHandledByFailureHandler() throws Exception {
        EffiRpcException failure = PredefinedErrorCode.COMMON.fail("stage failure");
        Fixture fixture = new Fixture(-1L);
        fixture.stageResult = Interaction.Result.failure(
                SmartURL.valueOf("http://127.0.0.1:8080/test"),
                failure
        );
        fixture.failureHandler = (context, failureCount, cause) -> {
            throw cause;
        };

        Result<String> result = fixture.newExecution().execute().await();

        assertTrue(result.failed());
        assertSame(failure, result.cause());
        assertEquals(0, fixture.attempts.get());
        assertEquals(1L, fixture.counter(CallerMetrics.CALL_COUNT.withTag("status", "failure")));
    }

    @Test
    void uncheckedFailureHandlerExceptionCompletesExecution() throws Exception {
        EffiRpcException failure = PredefinedErrorCode.COMMON.fail("stage failure");
        RuntimeException handlerFailure = new IllegalStateException("handler failure");
        Fixture fixture = new Fixture(-1L);
        fixture.stageResult = Interaction.Result.failure(
                SmartURL.valueOf("http://127.0.0.1:8080/test"),
                failure
        );
        fixture.failureHandler = (context, failureCount, cause) -> {
            throw handlerFailure;
        };

        Result<String> result = fixture.newExecution().execute().await();

        assertTrue(result.failed());
        assertSame(handlerFailure, result.cause().getCause());
        assertEquals(0, fixture.attempts.get());
    }

    private static void complete(
            ReplyFuture future,
            CallContext<Request, Caller<?>> context,
            String value,
            Response response
    ) {
        Interaction.Result result = Interaction.Result.success(context.message().url(), value);
        future.withRawResult(result);
        future.complete(new ReplyContext<>(context, response, result));
    }

    private static final class Fixture {

        private static final AtomicInteger PLATFORM_IDS = new AtomicInteger();

        private final ScopedPlatform platform = new ScopedPlatform(
                "call-execution-" + PLATFORM_IDS.incrementAndGet()
        );

        private final ScopedModule module;

        private final Response response;

        private final Request request;

        private final Caller<String> caller;

        private final AtomicInteger attempts = new AtomicInteger();

        private final AtomicReference<ReplyFuture> lastAttempt = new AtomicReference<>();

        private final CallerMetrics metrics = new CallerMetrics("test");

        private final DefaultMetrics registry;

        private Unary.FailureHandler failureHandler;

        private AttemptFactory attemptFactory;

        private Interaction.Result stageResult;

        private Fixture(long timeoutMillis) {
            Scheduler scheduler = new Scheduler();
            platform.registry().register(Scheduler.class, scheduler);
            platform.registry().register(CallFutureRegistry.class, new CallFutureRegistry());
            this.registry = new DefaultMetrics(platform);
            this.registry.register(metrics);

            ScopedApplication application = platform.newApplication("application");
            this.module = application.newModule("module");
            this.response = proxy(Response.class, (proxy, method, args) -> defaultValue(method.getReturnType()));

            SmartURL url = SmartURL.valueOf("http://127.0.0.1:8080/test");
            this.request = proxy(Request.class, (proxy, method, args) -> switch (method.getName()) {
                case "url" -> url;
                case "needReply" -> true;
                default -> defaultValue(method.getReturnType());
            });

            Stage.Chain stageChain = proxy(Stage.Chain.class, (proxy, method, args) -> {
                if (stageResult != null) {
                    return stageResult;
                }
                CallContext<Request, Caller<?>> context = (CallContext<Request, Caller<?>>) args[0];
                ReplyFuture future = attemptFactory.create(context, attempts.incrementAndGet());
                lastAttempt.set(future);
                return Interaction.Result.success(context.message().url(), future);
            });

            Protocol protocol = proxy(Protocol.class, (proxy, method, args) -> {
                if ("createRequest".equals(method.getName())) {
                    return request;
                }
                return defaultValue(method.getReturnType());
            });

            this.caller = proxy(Caller.class, (proxy, method, args) -> switch (method.getName()) {
                case "module" -> module;
                case "platform" -> platform;
                case "protocol" -> protocol;
                case "callStageChain" -> stageChain;
                case "get" -> args[0] == CallerMetrics.KEY ? metrics : null;
                case "option" -> option(method.getReturnType(), (OptionName<?>) args[0], timeoutMillis);
                default -> defaultValue(method.getReturnType());
            });
        }

        private CallExecution<String> newExecution() {
            return new CallExecution<>(
                    caller,
                    new PositionalInvocation(new Object[0]),
                    failureHandler
            );
        }

        private ReplyFuture lastAttempt() throws InterruptedException {
            ReplyFuture future = lastAttempt.get();
            future.await();
            return future;
        }

        private long counter(MetricKey key) {
            return registry.counter(key.withTag("protocol", "test")).count();
        }

        private long timerCount(MetricKey key) {
            return registry.timer(key.withTag("protocol", "test")).snapshot().count();
        }
    }

    @FunctionalInterface
    private interface AttemptFactory {

        ReplyFuture create(CallContext<Request, Caller<?>> context, int attempt);
    }

    private static Object option(Class<?> returnType, OptionName<?> option, long timeoutMillis) {
        if (option == TIMEOUT) {
            return Math.toIntExact(timeoutMillis);
        }
        return defaultValue(returnType);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive() || type == void.class) {
            return null;
        }
        if (type == boolean.class) {
            return false;
        }
        if (type == char.class) {
            return '\0';
        }
        if (type == byte.class) {
            return (byte) 0;
        }
        if (type == short.class) {
            return (short) 0;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type == float.class) {
            return 0F;
        }
        return 0D;
    }
}
