package io.effi.rpc.hook;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * Executes hooks before and after target operations.
 * <p>
 * Provides a mechanism to execute pre and post hooks around
 * operations, enabling customization and interception of
 * initialization, start, and close events.
 */
public class HookExecutor<H extends Hook<?>, T> {

    private static final HookExecutor<InitializeHook<Object>, Object> INITIALIZE =
            of(InitializeHook::onInitializing, InitializeHook::onInitialized);

    private static final HookExecutor<StartHook<Object>, Object> START =
            of(StartHook::onStarting, StartHook::onStarted);

    private static final HookExecutor<CloseHook<Object>, Object> CLOSE =
            of(CloseHook::onClosing, CloseHook::onClosed);

    public static <H extends InitializeHook<?>, T> HookExecutor<H, T> initialize() {
        return cast(INITIALIZE);
    }

    public static <H extends StartHook<?>, T> HookExecutor<H, T> start() {
        return cast(START);
    }

    public static <H extends CloseHook<?>, T> HookExecutor<H, T> close() {
        return cast(CLOSE);
    }

    public static <H extends Hook<T>, T> HookExecutor<H, T> of(BiConsumer<H, T> before, BiConsumer<H, T> after) {
        return new HookExecutor<>(before, after);
    }

    @SuppressWarnings("unchecked")
    private static <H extends Hook<?>, T> HookExecutor<H, T> cast(HookExecutor<?, ?> executor) {
        return (HookExecutor<H, T>) executor;
    }

    private final BiConsumer<H, T> before;
    private final BiConsumer<H, T> after;

    private HookExecutor(BiConsumer<H, T> before, BiConsumer<H, T> after) {
        this.before = before;
        this.after = after;
    }

    public <R> R execute(Iterable<? extends H> hooks, T target, Supplier<R> supplier) {
        hooks.forEach(hook -> before.accept(hook, target));
        R result = supplier.get();
        hooks.forEach(hook -> after.accept(hook, target));
        return result;
    }

    public void execute(Iterable<? extends H> hooks, T target, Runnable runnable) {
        hooks.forEach(hook -> before.accept(hook, target));
        runnable.run();
        hooks.forEach(hook -> after.accept(hook, target));
    }
}
