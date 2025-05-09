package io.effi.rpc.registry.consul;

import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.exception.PredefinedErrorCode;
import io.effi.rpc.registry.AbstractRegistryService;
import io.effi.rpc.registry.RegisterTask;
import io.effi.rpc.util.GenericKey;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;
import io.effi.rpc.util.VertxUtil;
import io.vertx.core.Vertx;
import io.vertx.ext.consul.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Implements {@link io.effi.rpc.registry.RegistryService} using Consul.
 * <p>
 * Handles service registration and discovery via the Consul registry.
 * See <a href="https://github.com/vert-x3/vertx-consul-client">vertx-consul-client</a> for details.
 * </p>
 */
public class ConsulRegistryService extends AbstractRegistryService {

    private ConsulClient consulClient;

    protected ConsulRegistryService(EffRpcApplication application, URL url) {
        super(application, url);
    }

    @Override
    public boolean isActive() {
        try {
            VertxUtil.await(consulClient.agentInfo());
            return true;
        } catch (Throwable e) {
            throw PredefinedErrorCode.CONNECT.fail(e, registryUrl.authority());
        }
    }

    @Override
    public void connect(URL url) {
        int connectTimeout = url.getIntParam(DefaultConfigKeys.CONNECT_TIMEOUT.key(),
                Constant.DEFAULT_CONNECT_TIMEOUT);
        try {
            ConsulClientOptions options = new ConsulClientOptions()
                    .setHost(url.host())
                    .setPort(url.port())
                    .setTimeout(connectTimeout);
            consulClient = ConsulClient.create(vertx(), options);
            // try to connect consul
            isActive();
        } catch (Throwable e) {
            throw PredefinedErrorCode.CONNECT.fail(e, url.authority());
        }
    }

    @Override
    public BiConsumer<RegisterTask, Map<String, String>> buildRegisterTask(String serviceName, URL url) {
        String serviceId = instanceId(url);
        return (registerTask, metaData) -> {
            ServiceOptions opts = new ServiceOptions()
                    .setName(serviceName)
                    .setId(serviceId)
                    .setAddress(url.host())
                    .setPort(url.port())
                    .setMeta(metaData);
            if (enableHealthCheck) {
                int healthCheckInterval = url.getIntParam(DefaultConfigKeys.HEALTH_CHECK_INTERVAL);
                CheckOptions checkOpts = new CheckOptions()
                        .setTcp(url.address())
                        .setId(serviceId)
                        .setDeregisterAfter((healthCheckInterval * 10) + "ms")
                        .setInterval(healthCheckInterval + "ms");
                opts.setCheckOptions(checkOpts);
            }
            try {
                VertxUtil.await(consulClient.registerService(opts));
                if (registerTask.isFirstRun()) registerTask.firstRun(false);
            } catch (Throwable e) {
                throw PredefinedErrorCode.REGISTRY_REGISTER.fail(e, serviceName, registryUrl.address());
            }
        };
    }

    @Override
    public void doDeregister(String serviceName, URL url) throws Throwable {
        String serviceId = instanceId(url);
        VertxUtil.await(consulClient.deregisterService(serviceId));
    }

    @Override
    protected List<URL> doDiscover(String serviceName, URL url) throws Throwable {
        ArrayList<URL> urls = new ArrayList<>();
        // Get the urls of all nodes for health checks
        ServiceEntryList result = VertxUtil.await(consulClient.healthServiceNodes(serviceName, true));
        List<ServiceEntry> serviceEntries = result.getList();
        for (ServiceEntry entry : serviceEntries) {
            Service service = entry.getService();
            String protocol = service.getMeta().get(KeyConstant.PROTOCOL);
            if (!StringUtil.isBlank(protocol) && protocol.equalsIgnoreCase(url.protocol())) {
                urls.add(serviceEntryToURL(entry));
            }
        }
        return urls;
    }

    @Override
    protected void doSubscribe(String serviceName, URL url) throws Throwable {
        Watch.service(serviceName, vertx()).setHandler(res -> {
            if (res.succeeded()) {
                List<ServiceEntry> serviceEntries = res.nextResult().getList();
                List<String> healthServerUrls = serviceEntries.stream()
                        .filter(instance -> instance.aggregatedStatus() == CheckStatus.PASSING
                                && instance.getService().getMeta().containsKey(KeyConstant.PROTOCOL))
                        .map(instance -> serviceEntryToURL(instance).toString())
                        .toList();
                discoverHealthServices.put(serviceName, healthServerUrls);
            }
        }).start();
    }

    @Override
    public void doClose() throws Throwable {
        consulClient.close();
    }

    private Vertx vertx() {
        return application.computeIfAbsent(GenericKey.valueOf(KeyConstant.VERTX), Vertx::vertx);
    }

    private URL serviceEntryToURL(ServiceEntry entry) {
        Service service = entry.getService();
        Map<String, String> meta = service.getMeta();
        String protocol = meta.get(KeyConstant.PROTOCOL).toLowerCase();
        return URL.builder()
                .protocol(protocol)
                .address(NetUtil.toAddress(service.getAddress(), service.getPort()))
                .params(meta)
                .build();
    }
}
