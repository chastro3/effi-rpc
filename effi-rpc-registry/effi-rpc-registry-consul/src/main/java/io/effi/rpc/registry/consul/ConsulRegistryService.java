package io.effi.rpc.registry.consul;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.registry.AbstractRegistryService;
import io.effi.rpc.util.NetUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.consul.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Implements {@link io.effi.rpc.registry.RegistryService} using Consul.
 * <p>
 * Handles service registration and discovery via the Consul registry.
 * See <a href="https://github.com/vert-x3/vertx-consul-client">vertx-consul-client</a> for details.
 * </p>
 */
public class ConsulRegistryService extends AbstractRegistryService {

    private static final Vertx vertx = Vertx.vertx();

    private final ConsulClient consulClient;

    protected ConsulRegistryService(RegistryConfig config) {
        // todo 提供vertx 的 consul config
        super(config);
        this.consulClient = createConsulClient(config);
    }

    @Override
    public boolean isActive() {
        try {
            toVoidFuture(consulClient.agentInfo()).join();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public RegistrationAction createRegistrationAction(String serviceName, ServiceHost serviceHost) {
        URL url = serviceHost.url();
        String instanceId = serviceHost.id();
        ServiceOptions opts = new ServiceOptions()
                .setName(serviceName)
                .setId(instanceId)
                .setAddress(url.host())
                .setPort(url.port());
        int heartbeatInterval = config.getIntParam(DefaultConfigKeys.HEARTBEAT_INTERVAL);
        CheckOptions checkOpts = new CheckOptions()
                .setId(instanceId)
                .setTtl((heartbeatInterval * 2) + "ms")
                .setDeregisterAfter((heartbeatInterval * 10) + "ms");
        opts.setCheckOptions(checkOpts);
        return (metaData) -> {
            opts.setMeta(metaData);
            return toVoidFuture(consulClient.registerService(opts))
                    .thenCompose(v -> toVoidFuture(consulClient.passCheck(instanceId)));
        };
    }

    @Override
    public CompletableFuture<Void> doDeregister(String serviceName, ServiceHost serviceHost) {
        String instanceId = serviceHost.id();
        return toVoidFuture(consulClient.deregisterService(instanceId));
    }

    @Override
    protected CompletableFuture<List<URL>> doDiscover(String serviceName, EffiRpcModule module) {
        // Get the urls of all nodes for health checks
        CompletableFuture<List<URL>> future = new CompletableFuture<>();
        consulClient.healthServiceNodes(serviceName, true)
                .onSuccess(serviceEntries -> {
                    List<URL> urls = serviceEntries.getList().stream().map(this::serviceEntryToURL).toList();
                    future.complete(urls);
                })
                .onFailure(future::completeExceptionally);
        return future;
    }

    @Override
    protected void doSubscribe(String serviceName) throws Throwable {
        Watch.service(serviceName, vertx).setHandler(res -> {
            if (res.succeeded()) {
                List<ServiceEntry> serviceEntries = res.nextResult().getList();
                List<URL> healthServerUrls = serviceEntries.stream()
                        .filter(instance ->
                                instance.aggregatedStatus() == CheckStatus.PASSING
                                        && instance.getService().getMeta().containsKey(KeyConstant.PROTOCOL))
                        .map(this::serviceEntryToURL).toList();
                onServicesUpdated(serviceName, healthServerUrls);
            }
        }).start();
    }

    @Override
    public void doClose() throws Throwable {
        consulClient.close();
    }

    private ConsulClient createConsulClient(RegistryConfig config) {
        int connectTimeout = config.getIntParam(DefaultConfigKeys.CONNECT_TIMEOUT);
        URL url = config.url();
        ConsulClientOptions options = new ConsulClientOptions()
                .setHost(url.host())
                .setPort(url.port())
                .setTimeout(connectTimeout);
        return ConsulClient.create(vertx, options);
    }

    private URL serviceEntryToURL(ServiceEntry entry) {
        Service service = entry.getService();
        Map<String, String> meta = service.getMeta();
        String protocol = meta.get(KeyConstant.PROTOCOL).toLowerCase();
        return URL.builder().protocol(protocol).address(NetUtil.toAddress(service.getAddress(), service.getPort())).params(meta).build();
    }

    private CompletableFuture<Void> toVoidFuture(Future<?> future) {
        CompletableFuture<Void> result = new CompletableFuture<>();
        future.onSuccess(v -> result.complete(null))
                .onFailure(result::completeExceptionally);
        return result;
    }
}
