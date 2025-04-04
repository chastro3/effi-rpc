package io.effi.rpc.engine;

import io.effi.rpc.common.constant.DefaultConfigKeys;
import io.effi.rpc.common.url.Config;
import io.effi.rpc.common.url.QueryPath;
import io.effi.rpc.common.util.AbstractAttributes;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.TypeToken;
import io.effi.rpc.contract.Invoker;
import io.effi.rpc.contract.ThreadPool;
import io.effi.rpc.contract.manager.ThreadPoolManager;
import io.effi.rpc.contract.module.EffiRpcModule;
import io.effi.rpc.engine.builder.InvokerBuilder;
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

    public Protocol protocolInstance() {
        return protocol;
    }

    protected ThreadPool getThreadPool(EffiRpcModule module, String defaultThreadPoolName) {
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
