package io.effi.rpc.contract.filter;

import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.Result;
import io.effi.rpc.contract.context.ExecutorContext;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.Attributes;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.Messages;

import java.util.List;
import java.util.function.Supplier;

/**
 * Manages a chain of filters for processing an RPC invocation.
 * <p>
 * Executes filters in order to handle concerns like logging, authentication, and validation.
 * Filters can modify the invocation or its result, and control whether to proceed to the next filter.
 * </p>
 */
public class FilterChain {

    private static final Logger logger = LoggerFactory.getLogger(FilterChain.class);

    /**
     * Executes the filter chain on the given context.
     *
     * @param context the execution context for the filter chain
     * @param filters the filters to apply
     * @param <C>     the type of the execution context
     * @return the result of the execution
     */
    @SuppressWarnings("unchecked")
    public static <C extends ExecutorContext<?, ?, C>> Result execute(C context, List<? extends Filter<?, ?, ?>> filters) {
        ExecutorContext<?, ?, ?> filterContext;
        switch (context) {
            case InvocationContext<?, ?> invocationContext ->
                    filterContext = new FilterInvocationContext<>(invocationContext);
            case ReplyContext<?, ?> replyContext -> filterContext = new FilterReplyContext<>(replyContext);
            default -> throw new IllegalArgumentException(Messages.unSupport("context", context.getClass()));
        }
        return doExecute(context, (ExecutorContext<?, ?, C>) filterContext, filters, 0);
    }

    @SuppressWarnings("unchecked")
    private static <C extends ExecutorContext<?, ?, C>> Result doExecute(C context, ExecutorContext<?, ?, C> filterContext, List<? extends Filter<?, ?, ?>> filters, int index) {
        if (CollectionUtil.isEmpty(filters) || index == filters.size()) {
            return context.execute();
        }
        Filter<?, ?, C> filter = (Filter<?, ?, C>) filters.get(index);
        //logger.debug("Invoking Filter :" + filter.getClass());
        return filter.doFilter(filterContext.executor(() -> doExecute(context, filterContext, filters, index + 1)));
    }

    private static class FilterInvocationContext<R extends Envelope.Request, I extends Invoker<?>>
            extends InvocationContext<R, I> {

        private final InvocationContext<R, I> context;

        FilterInvocationContext(InvocationContext<R, I> context) {
            super(context.module(), context.envelope(), context.invoker(), context.args());
            this.context = context;
        }

        @Override
        public <T> T get(GenericKey<T> key) {
            return context.get(key);
        }

        @Override
        public <T> T getOrDefault(GenericKey<T> key, T defaultValue) {
            return context.getOrDefault(key, defaultValue);
        }

        @Override
        public <T> T computeIfAbsent(GenericKey<T> key, Supplier<T> creator) {
            return context.computeIfAbsent(key, creator);
        }

        @Override
        public <T> T set(GenericKey<T> key, T value) {
            return context.set(key, value);
        }

        @Override
        public Attributes remove(GenericKey<?> key) {
            return context.remove(key);
        }

        @Override
        public void clear() {
            context.clear();
        }
    }

    private static class FilterReplyContext<R extends Envelope.Response, I extends Invoker<?>>
            extends ReplyContext<R, I> {

        private final ReplyContext<R, I> context;

        FilterReplyContext(ReplyContext<R, I> context) {
            super(context.invocationContext(), context.envelope(), context.result());
            this.context = context;
        }

        @Override
        public <T> T get(GenericKey<T> key) {
            return context.get(key);
        }

        @Override
        public <T> T getOrDefault(GenericKey<T> key, T defaultValue) {
            return context.getOrDefault(key, defaultValue);
        }

        @Override
        public <T> T computeIfAbsent(GenericKey<T> key, Supplier<T> creator) {
            return context.computeIfAbsent(key, creator);
        }

        @Override
        public <T> T set(GenericKey<T> key, T value) {
            return context.set(key, value);
        }

        @Override
        public Attributes remove(GenericKey<?> key) {
            return context.remove(key);
        }

        @Override
        public void clear() {
            context.clear();
        }
    }

}


