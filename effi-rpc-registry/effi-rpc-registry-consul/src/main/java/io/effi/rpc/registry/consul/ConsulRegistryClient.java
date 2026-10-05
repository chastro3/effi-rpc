package io.effi.rpc.registry.consul;

import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.component.registry.options.RegistryOptions;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.concurrent.Promise;
import io.effi.rpc.exception.PredefinedErrorCode;
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
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implements {@link RegistryClient} using Consul.
 * <p>
 * Watches are created with the configured client options; see
 * <a href="https://github.com/vert-x3/vertx-consul-client">vertx-consul-client</a> for details.
 */
public class ConsulRegistryClient extends AbstractRegistryClient {

    private final Vertx vertx = Vertx.vertx();

    private final ConsulClientOptions consulOptions;

    private final ConsulClient consulClient;

    private final Map<String, Watch<?>> watches = new ConcurrentHashMap<>();

    protected ConsulRegistryClient(RegistryConfig config, ScopedPlatform platform) {
        super(config, platform);
        this.consulOptions = createConsulOptions(config);
        this.consulClient = ConsulClient.create(vertx, consulOptions);
    }

    @Override
    public boolean active() {
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
    public Registration createRegistration(ServiceInstance instance) {
        String instanceId = instance.id();
        ServiceOptions opts = new ServiceOptions()
                .setName(instance.serviceName())
                .setId(instanceId)
                .setAddress(instance.host())
                .setPort(instance.port());
        int heartbeatInterval = config.option(RegistryOptions.HEARTBEAT_INTERVAL);
        CheckOptions checkOpts = new CheckOptions()
                .setId(instanceId)
                .setTtl((heartbeatInterval * 2) + "ms")
                .setDeregisterAfter((heartbeatInterval * 10) + "ms");
        opts.setCheckOptions(checkOpts);
        return (serviceInst) -> {
            opts.setMeta(serviceInst.metadata());
            Promise<Void> result = new Promise<>();
            toVoidFuture(consulClient.registerService(opts), "registerService").onComplete(registration -> {
                if (registration.failed()) {
                    result.complete(registration);
                    return;
                }
                toVoidFuture(consulClient.passCheck(instanceId), "passCheck").onComplete(result::complete);
            });
            return result;
        };
    }

    @Override
    public Promise<Void> doDeregister(ServiceInstance instance) {
        return toVoidFuture(consulClient.deregisterService(instance.id()), "deregisterService");
    }

    @Override
    public void doClose() {
        watches.values().forEach(Watch::stop);
        watches.clear();
        consulClient.close();
        vertx.close().onFailure(cause -> logger.error("Failed to close Vert.x for registry '{}'", cause, config));
    }

    @Override
    protected Promise<List<ServiceInstance>> doLookup(String serviceName) {
        Promise<List<ServiceInstance>> promise = new Promise<>();
        consulClient.healthServiceNodes(serviceName, true)
                .onSuccess(serviceEntries -> {
                    List<ServiceInstance> instances = serviceEntries.getList()
                            .stream()
                            .filter(this::hasProtocolMetadata)
                            .map(this::toServiceInstance)
                            .toList();
                    promise.success(instances);
                })
                .onFailure(cause -> promise.failure(
                        PredefinedErrorCode.REGISTRY_DISCOVER.fail(cause, serviceName, config)
                ));
        return promise;
    }

    @Override
    protected void doSubscribe(String serviceName) throws Throwable {
        Watch<?> watch = Watch.service(serviceName, vertx, consulOptions).setHandler(res -> {
            if (res.succeeded()) {
                List<ServiceEntry> serviceEntries = res.nextResult().getList();
                List<ServiceInstance> healthInstances = serviceEntries.stream()
                        .filter(instance ->
                                instance.aggregatedStatus() == CheckStatus.PASSING
                                        && hasProtocolMetadata(instance))
                        .map(this::toServiceInstance)
                        .toList();
                onServicesUpdated(serviceName, healthInstances);
            }
        });
        watches.put(serviceName, watch);
        watch.start();
    }

    private ConsulClientOptions createConsulOptions(RegistryConfig config) {
        int connectTimeout = config.option(RegistryOptions.CONNECT_TIMEOUT);
        String address = addresses[0];
        if (addresses.length > 1) {
            logger.warn("Consul registry '{}' declares {} addresses; only '{}' is used",
                    config.id(), addresses.length, address);
        }
        InetSocketAddress socketAddress = NetUtil.toInetSocketAddress(address.trim());
        return new ConsulClientOptions()
                .setHost(socketAddress.getHostString())
                .setPort(socketAddress.getPort())
                .setTimeout(connectTimeout);
    }

    private boolean hasProtocolMetadata(ServiceEntry entry) {
        Map<String, String> metadata = entry.getService().getMeta();
        return metadata != null && metadata.containsKey(KeyConstant.PROTOCOL);
    }

    private ServiceInstance toServiceInstance(ServiceEntry entry) {
        Service service = entry.getService();
        Map<String, String> meta = service.getMeta();
        String protocol = meta.get(KeyConstant.PROTOCOL).toLowerCase(Locale.ROOT);
        return DefaultServiceInstance.builder()
                .id(service.getId())
                .protocol(protocol)
                .serviceName(service.getName())
                .host(service.getAddress())
                .port(service.getPort())
                .addMetadata(meta)
                .build();
    }

    private Promise<Void> toVoidFuture(Future<?> future, String operation) {
        Promise<Void> result = new Promise<>();
        future.onSuccess(v -> result.success(null))
                .onFailure(cause -> result.failure(
                        ConsulErrorCodes.OPERATION_FAILED.fail(cause, operation)
                ));
        return result;
    }
}
