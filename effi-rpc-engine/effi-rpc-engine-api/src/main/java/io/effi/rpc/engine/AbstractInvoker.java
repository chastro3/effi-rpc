package io.effi.rpc.engine;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.QueryPath;
import io.effi.rpc.common.util.AbstractAttributes;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.Caller;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.ThreadPool;
import io.effi.rpc.contract.manager.ThreadPoolManager;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.engine.builder.InvokerBuilder;
import io.effi.rpc.metrics.CalleeMetrics;
import io.effi.rpc.metrics.CallerMetrics;
import io.effi.rpc.transport.Protocol;
import io.effi.rpc.transport.TransportSupport;

/**
 * Abstract implementation of {@link Invoker}.
 *
 * @param <R> the type of the result
 */
public abstract class AbstractInvoker<R> extends AbstractAttributes implements Invoker<R> {

    protected Config config;

    protected QueryPath queryPath;

    protected TypeToken<?> returnType;

    protected Protocol protocol;

    protected AbstractInvoker(Config config, InvokerBuilder<?, ?> builder) {
        this.config = AssertUtil.notNull(config, "config");
        String path = config.get(DefaultConfigKeys.PATH);
        this.queryPath = path == null ? QueryPath.EMPTY_PATH : QueryPath.valueOf(path.replace(",", "/"));
        this.returnType = builder.returnType();
        this.protocol = TransportSupport.getProtocol(builder.protocol());
    }

    @Override
    public Config config() {
        return config;
    }

    @Override
    public QueryPath queryPath() {
        return queryPath;
    }

    @Override
    public TypeToken<?> returnType() {
        return returnType;
    }

    @Override
    public String protocol() {
        return protocol.protocol();
    }

    /**
     * Returns the protocol instance.
     */
    public Protocol protocolInstance() {
        return protocol;
    }

    /**
     * Determines whether serialization is occurring in an I/O thread.
     * This is based on the configured serialization threshold and the
     * observed average serialization time.
     *
     * @return {@code true} if serialization is happening in an I/O thread, otherwise {@code false}
     */
    public boolean inIOSerialization() {
        try {
            String value = this.get(DefaultConfigKeys.SERIALIZATION_THRESHOLD);
            long serializationThreshold = Long.parseLong(value);
            if (serializationThreshold == 0) return true;
            double averageSerializationTime;
            if (this instanceof Caller<?>) {
                CallerMetrics callerMetrics = this.get(CallerMetrics.GENERIC_KEY);
                averageSerializationTime = callerMetrics.averageSerializationTime().get();
            } else {
                CalleeMetrics calleeMetrics = this.get(CalleeMetrics.GENERIC_KEY);
                averageSerializationTime = calleeMetrics.averageSerializationTime().get();
            }
            return averageSerializationTime < serializationThreshold;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    /**
     * Determines whether deserialization is occurring in an I/O thread.
     * This is based on the configured deserialization threshold and the
     * observed average deserialization time.
     *
     * @return {@code true} if deserialization is happening in an I/O thread, otherwise {@code false}
     */
    public boolean inIODeserialization() {
        try {
            String value = this.get(DefaultConfigKeys.DESERIALIZATION_THRESHOLD);
            long deserializationThreshold = Long.parseLong(value);
            if (deserializationThreshold == 0) return true;
            double averageDeserializationTime;
            if (this instanceof Caller<?>) {
                CallerMetrics callerMetrics = this.get(CallerMetrics.GENERIC_KEY);
                averageDeserializationTime = callerMetrics.averageDeserializationTime().get();
            } else {
                CalleeMetrics calleeMetrics = this.get(CalleeMetrics.GENERIC_KEY);
                averageDeserializationTime = calleeMetrics.averageDeserializationTime().get();
            }
            return averageDeserializationTime < deserializationThreshold;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    protected ThreadPool threadPool(EffiRpcModule module, String defaultThreadPoolName) {
        String threadPoolName = this.get(DefaultConfigKeys.THREAD_POOL);
        ThreadPoolManager threadPoolManager = module.threadPoolManager();
        ThreadPool threadPool = threadPoolManager.get(threadPoolName);
        return threadPool != null ? threadPool : threadPoolManager.get(defaultThreadPoolName);
    }

    @Override
    public String toString() {
        return queryPath.toString();
    }
}
