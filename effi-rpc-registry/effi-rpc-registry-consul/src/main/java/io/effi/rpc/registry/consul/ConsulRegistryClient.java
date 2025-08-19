package io.effi.rpc.registry.consul;

import io.effi.rpc.async.Promise;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.registry.AbstractRegistryClient;
import io.effi.rpc.registry.DefaultServiceInstance;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.NetUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.consul.CheckOptions;
import io.vertx.ext.consul.CheckStatus;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.ConsulClientOptions;
import io.vertx.ext.consul.Service;
import io.vertx.ext.consul.ServiceEntry;
import io.vertx.ext.consul.ServiceOptions;
import io.vertx.ext.consul.Watch;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * Implements {@link RegistryClient} using Consul.
 * <p>
 * Handles service registration and discovery via the Consul registry.
 * See <a href="https://github.com/vert-x3/vertx-consul-client">vertx-consul-client</a> for details.
 */
public class ConsulRegistryClient extends AbstractRegistryClient {

    private static final Vertx vertx = Vertx.vertx();

    private final ConsulClient consulClient;

    protected ConsulRegistryClient(RegistryConfig config, ScopedPlatform platform) {
        // todo 提供vertx 的 consul config
        super(config, platform, true);
        this.consulClient = createConsulClient(config);
    }

    @Override
    public boolean isActive() {
        try {
            consulClient.agentInfo()
                    .toCompletionStage()
                    .toCompletableFuture().join();
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    @Override
    public Registration createRegistrationAction(ServiceInstance instance) {
        String instanceId = instance.id();
        ServiceOptions opts = new ServiceOptions()
                .setName(instance.serviceName())
                .setId(instanceId)
                .setAddress(instance.host())
                .setPort(instance.port());
        int heartbeatInterval = config.getConfig(ConfigNames.HEARTBEAT_INTERVAL);
        CheckOptions checkOpts = new CheckOptions()
                .setId(instanceId)
                .setTtl((heartbeatInterval * 2) + "ms")
                .setDeregisterAfter((heartbeatInterval * 10) + "ms");
        opts.setCheckOptions(checkOpts);
        return (serviceInst) -> {
            opts.setMeta(serviceInst.metadata());
            return toVoidFuture(consulClient.registerService(opts))
                    .onComplete(res -> toVoidFuture(consulClient.passCheck(instanceId)));
        };
    }

    @Override
    public Promise<Void> doDeregister(ServiceInstance instance) {
        return toVoidFuture(consulClient.deregisterService(instance.id()));
    }

    @Override
    protected Promise<List<ServiceInstance>> doLookup(String serviceName) {
        // Get the urls of all nodes for health checks
        Promise<List<ServiceInstance>> promise = new Promise<>();
        consulClient.healthServiceNodes(serviceName, true)
                .onSuccess(serviceEntries -> {
                    List<ServiceInstance> instances = serviceEntries.getList()
                            .stream()
                            .map(this::toServiceInstance)
                            .toList();
                    promise.success(instances);
                })
                .onFailure(promise::failure);
        return promise;
    }

    @Override
    protected void doSubscribe(String serviceName) throws Throwable {
        Watch.service(serviceName, vertx).setHandler(res -> {
            if (res.succeeded()) {
                List<ServiceEntry> serviceEntries = res.nextResult().getList();
                List<ServiceInstance> healthInstances = serviceEntries.stream()
                        .filter(instance ->
                                instance.aggregatedStatus() == CheckStatus.PASSING
                                        && instance.getService().getMeta().containsKey(KeyConstant.PROTOCOL))
                        .map(this::toServiceInstance).toList();
                onServicesUpdated(serviceName, healthInstances);
            }
        }).start();
    }

    @Override
    public void doClose() throws Throwable {
        consulClient.close();
    }

    private ConsulClient createConsulClient(RegistryConfig config) {
        int connectTimeout = config.getConfig(ConfigNames.CONNECT_TIMEOUT);
        String address = addresses[0];
        InetSocketAddress socketAddress = NetUtil.toInetSocketAddress(address);
        ConsulClientOptions options = new ConsulClientOptions()
                .setHost(socketAddress.getHostString())
                .setPort(socketAddress.getPort())
                .setTimeout(connectTimeout);
        return ConsulClient.create(vertx, options);
    }

    private ServiceInstance toServiceInstance(ServiceEntry entry) {
        Service service = entry.getService();
        Map<String, String> meta = service.getMeta();
        String protocol = meta.get(KeyConstant.PROTOCOL).toLowerCase();
        return DefaultServiceInstance.builder()
                .id(service.getId())
                .protocol(protocol)
                .serviceName(service.getName())
                .host(service.getAddress())
                .port(service.getPort())
                .addMetadata(meta)
                .build();
    }

    private Promise<Void> toVoidFuture(Future<?> future) {
        Promise<Void> result = new Promise<>();
        future.onSuccess(v -> result.success(null))
                .onFailure(result::failure);
        return result;
    }
}
