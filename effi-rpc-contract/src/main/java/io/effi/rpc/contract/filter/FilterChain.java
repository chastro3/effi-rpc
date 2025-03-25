package io.effi.rpc.contract.filter;

import io.effi.rpc.common.extension.Attributes;
import io.effi.rpc.common.extension.GenericKey;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.Messages;
import io.effi.rpc.contract.Envelope;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.Result;
import io.effi.rpc.contract.context.ExecutorContext;
import io.effi.rpc.contract.context.InvocationContext;
import io.effi.rpc.contract.context.ReplyContext;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;

import java.util.List;
import java.util.function.Supplier;

/**
 * Manage a chain of filters to process an RPC invocation.
 * <p>
 * The filter chain executes filters in a specified order,
 * allowing concerns like logging, authentication,and validation
 * to be applied during the invocation process.
 * Filters can modify the invocation or its result, and
 * control whether to proceed to the next filter.
 * </p>
 */
public class FilterChain {

    private static final Logger logger = LoggerFactory.getLogger(FilterChain.class);

    /**
     * Executes the filter chain on the provided context.
     *
     * @param context the execution context for the filter chain
     * @param filters the list of filters to be applied
     * @param <C>     the type of the execution context
     * @return the result of the executed operation
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
        logger.debug("Invoking Filter :" + filter.getClass());
        return filter.doFilter(filterContext.executor(() -> doExecute(context, filterContext, filters, index + 1)));
    }

    private static class FilterInvocationContext<T extends Envelope.Request, I extends Invoker<?>>
            extends InvocationContext<T, I> {

        private final InvocationContext<T, I> context;

        FilterInvocationContext(InvocationContext<T, I> context) {
            super(context.module(), context.source(), context.invoker(), context.args());
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

    private static class FilterReplyContext<T extends Envelope.Response, I extends Invoker<?>>
            extends ReplyContext<T, I> {

        private final ReplyContext<T, I> context;

        FilterReplyContext(ReplyContext<T, I> context) {
            super(context.invocationContext(), context.source(), context.result());
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


