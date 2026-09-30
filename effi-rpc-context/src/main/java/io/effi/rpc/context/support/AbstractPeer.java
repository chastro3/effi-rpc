package io.effi.rpc.context.support;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.tools.ThreadPool;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.context.InteractionErrorCodes;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.InterceptorChainResolver;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.PeerDescriptor;
import io.effi.rpc.context.PeerGroup;
import io.effi.rpc.context.Protocol;
import io.effi.rpc.context.Stage;
import io.effi.rpc.context.StageChainResolver;
import io.effi.rpc.context.ThreadPoolResolver;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.trait.FluentBuilder;
import io.effi.rpc.util.AbstractAttributes;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.TypeCapture;

import java.util.Arrays;

import static io.effi.rpc.component.serialization.options.CompressionOptions.COMPRESSOR;
import static io.effi.rpc.context.options.PeerOptions.PATH;
import static io.effi.rpc.context.options.ResolverOptions.INTERCEPTOR_CHAIN_RESOLVER;
import static io.effi.rpc.context.options.ResolverOptions.STAGE_CHAIN_RESOLVER;
import static io.effi.rpc.context.options.ResolverOptions.THREAD_POOL_RESOLVER;
import static io.effi.rpc.context.options.SerializationOptions.SERIALIZER;

/**
 * Provides an immutable implementation of {@link Peer}.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractPeer<B extends AbstractPeer.Builder> extends AbstractAttributes implements Peer {

    protected final String id;

    protected final PeerDescriptor descriptor;

    protected final ScopedModule module;

    protected final ThreadPool threadPool;

    protected final Stage.Chain callStageChain;

    protected final Stage.Chain replyStageChain;

    protected final Interceptor.Chain callInterceptorChain;

    protected final Interceptor.Chain replyInterceptorChain;

    protected AbstractPeer(B builder) {
        this.module = builder.module;
        this.descriptor = builder.descriptor;
        this.threadPool = builder.threadPool;
        this.callStageChain = builder.callStageChain;
        this.replyStageChain = builder.replyStageChain;
        this.callInterceptorChain = builder.callInterceptorChain;
        this.replyInterceptorChain = builder.replyInterceptorChain;
        this.id = Peer.buildId(descriptor.protocol().name(), descriptor.path().path());
        this.descriptor.options().withOwner(this);
    }

    @Override
    public HierarchicalOptions options() {
        return descriptor.options();
    }

    @Override
    public QueryPath queryPath() {
        return descriptor.path();
    }

    @Override
    public Protocol protocol() {
        return descriptor.protocol();
    }

    @Override
    public ScopedModule module() {
        return module;
    }

    @Override
    public ThreadPool threadPool() {
        return threadPool;
    }

    @Override
    public TypeCapture<?> replyType() {
        return descriptor.replyType();
    }

    @Override
    public Stage.Chain callStageChain() {
        return callStageChain;
    }

    @Override
    public Stage.Chain replyStageChain() {
        return replyStageChain;
    }

    @Override
    public Interceptor.Chain callInterceptorChain() {
        return callInterceptorChain;
    }

    @Override
    public Interceptor.Chain replyInterceptorChain() {
        return replyInterceptorChain;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String toString() {
        return queryPath().toString();
    }

    /**
     * Assembles a complete {@link Peer} before publishing it to its module.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public abstract static class Builder<T extends Peer, SELF extends Builder<T, SELF>>
            implements FluentBuilder<T, SELF>, HierarchicalOptions.Supplier, ScopedModule.Supplier {

        protected final String protocolName;

        protected HierarchicalOptions options = HierarchicalOptions.create();

        protected ScopedModule module;

        protected TypeCapture<?> replyType;

        protected Protocol protocol;

        protected PeerDescriptor descriptor;

        protected ThreadPool threadPool;

        protected Stage.Chain callStageChain;

        protected Stage.Chain replyStageChain;

        protected Interceptor.Chain callInterceptorChain;

        protected Interceptor.Chain replyInterceptorChain;

        protected Builder(String protocol) {
            this.protocolName = AssertUtil.notBlank(protocol, "protocol");
        }

        public SELF options(HierarchicalOptions options) {
            this.options = AssertUtil.notNull(options, "options");
            return self();
        }

        public SELF path(String path) {
            addOption(PATH, new String[]{path});
            return self();
        }

        public SELF module(ScopedModule module) {
            this.module = module;
            return self();
        }

        public SELF compressor(String compressor) {
            addOption(COMPRESSOR, compressor);
            return self();
        }

        public SELF serializer(String serializer) {
            addOption(SERIALIZER, serializer);
            return self();
        }

        @Override
        public HierarchicalOptions options() {
            return options;
        }

        public Protocol protocol() {
            return protocol;
        }

        public ThreadPool threadPool() {
            return threadPool;
        }

        public SELF threadPool(ThreadPool threadPool) {
            this.threadPool = AssertUtil.notNull(threadPool, "threadPool");
            return self();
        }

        public Stage.Chain callStageChain() {
            return callStageChain;
        }

        public SELF callStageChain(Stage.Chain chain) {
            this.callStageChain = AssertUtil.notNull(chain, "chain");
            return self();
        }

        public Stage.Chain replyStageChain() {
            return replyStageChain;
        }

        public SELF replyStageChain(Stage.Chain chain) {
            this.replyStageChain = AssertUtil.notNull(chain, "chain");
            return self();
        }

        public Interceptor.Chain callInterceptorChain() {
            return callInterceptorChain;
        }

        public SELF callInterceptorChain(Interceptor.Chain chain) {
            this.callInterceptorChain = AssertUtil.notNull(chain, "chain");
            return self();
        }

        public Interceptor.Chain replyInterceptorChain() {
            return replyInterceptorChain;
        }

        public SELF replyInterceptorChain(Interceptor.Chain chain) {
            this.replyInterceptorChain = AssertUtil.notNull(chain, "chain");
            return self();
        }

        @Override
        public ScopedModule module() {
            return module;
        }

        @Override
        public final T build() {
            validate();
            resolve();
            prepare();
            resolveComponents();
            checkState();
            T peer = newInstance();
            module.registry().register((Class<T>) peerType(), peer);
            PeerGroup<?, ?> group = group();
            if (group != null) {
                ((PeerGroup) group).register(peer);
            }
            return peer;
        }

        protected abstract Class<? extends Peer> peerType();

        protected abstract PeerDescriptor.Kind kind();

        protected abstract T newInstance();

        protected PeerGroup<?, ?> group() {
            return null;
        }

        protected void prepare() {
        }

        protected void resolveComponents() {
            if (threadPool == null) {
                threadPool = module.preferredExtension(ThreadPoolResolver.class, option(THREAD_POOL_RESOLVER)).resolve(descriptor, module);
            }
            if (callStageChain == null || replyStageChain == null) {
                StageChainResolver resolver = module.preferredExtension(StageChainResolver.class, option(STAGE_CHAIN_RESOLVER));
                if (callStageChain == null) {
                    callStageChain = resolver.resolveCallChain(descriptor, module);
                }
                if (replyStageChain == null) {
                    replyStageChain = resolver.resolveReplyChain(descriptor, module);
                }
            }
            if (callInterceptorChain == null || replyInterceptorChain == null) {
                InterceptorChainResolver resolver = module.preferredExtension(
                        InterceptorChainResolver.class,
                        option(INTERCEPTOR_CHAIN_RESOLVER)
                );
                if (callInterceptorChain == null) {
                    callInterceptorChain = resolver.resolveCallChain(descriptor, module);
                }
                if (replyInterceptorChain == null) {
                    replyInterceptorChain = resolver.resolveReplyChain(descriptor, module);
                }
            }
        }

        protected void validate() {
            AssertUtil.notNull(module, "module");
            AssertUtil.notNull(replyType, "replyType");
        }

        protected void checkState() {
            AssertUtil.notNull(threadPool, "threadPool");
            AssertUtil.notNull(callStageChain, "callStageChain");
            AssertUtil.notNull(replyStageChain, "replyStageChain");
            AssertUtil.notNull(callInterceptorChain, "callInterceptorChain");
            AssertUtil.notNull(replyInterceptorChain, "replyInterceptorChain");
        }

        private void resolve() {
            this.protocol = module.platform().namedExtension(Protocol.class, protocolName);
            if (protocol == null) {
                throw InteractionErrorCodes.PROTOCOL_NOT_FOUND.fail(protocolName);
            }
            this.descriptor = new PeerDescriptor(kind(), protocol, queryPath(), replyType, options);
        }

        private QueryPath queryPath() {
            String[] pathSegments = option(PATH);
            return pathSegments == null || pathSegments.length == 0
                    ? QueryPath.empty()
                    : QueryPath.valueOf(Arrays.asList(pathSegments));
        }
    }
}
