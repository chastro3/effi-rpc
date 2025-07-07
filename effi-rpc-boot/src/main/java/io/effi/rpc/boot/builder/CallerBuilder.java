package io.effi.rpc.boot.builder;

import io.effi.rpc.base.Caller;
import io.effi.rpc.base.Locator;
import io.effi.rpc.base.RemoteClient;
import io.effi.rpc.base.context.InterceptorChain;
import io.effi.rpc.base.context.StageChain;
import io.effi.rpc.config.DefaultConfigNames;
import io.effi.rpc.config.NodeConfig;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.config.transport.ClientConfig;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.TypeToken;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds {@link Caller} instance and defines configuration.
 */
public abstract class CallerBuilder<T extends Caller<?>, C extends CallerBuilder<T, C>> extends CallSideBuilder<T, C> {

    protected Locator locator;

    protected ClientConfig clientConfig;

    protected List<RegistryConfig> registryConfigs;

    protected InterceptorChain chosenInterceptorChain;

    protected StageChain replyStageChain;

    protected InterceptorChain replyInterceptorChain;

    protected CallerBuilder(TypeToken<?> returnType, NodeConfig config) {
        super(config);
        this.replyType = AssertUtil.notNull(returnType, "returnType");
        this.registryConfigs = new ArrayList<>();
    }

    public C directAddress(String address) {
        config.set(DefaultConfigNames.ADDRESS.realName(), address);
        return returnThis();
    }

    public C directAddress(InetSocketAddress address) {
        return directAddress(NetUtil.toAddress(address));
    }

    public C remoteApplication(String applicationName) {
        config.set(DefaultConfigNames.REMOTE_APPLICATION, applicationName);
        return returnThis();
    }

    public C registryConfigs(RegistryConfig... registryConfigs) {
        if (CollectionUtil.isNotEmpty(registryConfigs)) {
            CollectionUtil.addUnique(this.registryConfigs, registryConfigs);
        }
        return returnThis();
    }

    public C remoteModule(String moduleName) {
        config.set(DefaultConfigNames.REMOTE_MODULE, moduleName);
        return returnThis();
    }

    public C container(RemoteClient<?> client) {
        this.container = client;
        return returnThis();
    }

    public C locator(Locator locator) {
        this.locator = locator;
        return returnThis();
    }

    public C clientConfig(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        return returnThis();
    }

    public C chosenInterceptorChain(InterceptorChain chain) {
        this.chosenInterceptorChain = chain;
        return returnThis();
    }

    public C replyStageChain(StageChain chain) {
        this.replyStageChain = chain;
        return returnThis();
    }

    public C replyInterceptorChain(InterceptorChain chain) {
        this.replyInterceptorChain = chain;
        return returnThis();
    }

    public C retries(int retries) {
        config.set(DefaultConfigNames.RETRIES, String.valueOf(retries));
        return returnThis();
    }

    public C loadBalance(String loadBalance) {
        config.set(DefaultConfigNames.LOAD_BALANCE, loadBalance);
        return returnThis();
    }

    public C faultTolerance(String faultTolerance) {
        config.set(DefaultConfigNames.FAULT_TOLERANCE, faultTolerance);
        return returnThis();
    }

    public C timeout(int timeout) {
        config.set(DefaultConfigNames.TIMEOUT, String.valueOf(timeout));
        return returnThis();
    }

    public Locator locator() {
        return locator;
    }

    public ClientConfig clientConfig() {
        return clientConfig;
    }

    public List<RegistryConfig> registryConfigs() {
        return registryConfigs;
    }

    public InterceptorChain chosenInterceptorChain() {
        return chosenInterceptorChain;
    }

    public StageChain replyStageChain() {
        return replyStageChain;
    }

    public InterceptorChain replyInterceptorChain() {
        return replyInterceptorChain;
    }
}


