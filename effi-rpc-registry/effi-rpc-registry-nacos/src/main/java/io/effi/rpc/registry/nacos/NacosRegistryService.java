package io.effi.rpc.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.component.EffiRpcModule;
import io.effi.rpc.config.DefaultConfigKeys;
import io.effi.rpc.config.URL;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.registry.AbstractRegistryService;
import io.effi.rpc.util.NetUtil;
import io.effi.rpc.util.StringUtil;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * Implements {@link io.effi.rpc.registry.RegistryService} using Nacos.
 * <p>
 * See <a href="https://github.com/alibaba/nacos">Nacos</a> for details.
 * </p>
 */
public class NacosRegistryService extends AbstractRegistryService {

    private NamingService namingService;

    protected NacosRegistryService(RegistryConfig config) {
        super(config);
        String projectName = config.get(DefaultConfigKeys.NACOS_PROJECT_NAME);
        if (StringUtil.isNotBlank(projectName))
            // nacos <project.name>
            System.setProperty("project.name", projectName);
        try {
            this.namingService = NamingFactory.createNamingService(config.url().address());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isActive() {
        return namingService.getServerStatus().equals("UP");
    }

    @Override
    public RegistrationAction createRegistrationAction(String serviceName, ServiceHost serviceHost) {
        URL url = serviceHost.url();
        String instanceId = serviceHost.id();
        Instance instance = new Instance();
        instance.setInstanceId(instanceId);
        instance.setIp(url.host());
        instance.setPort(url.port());
        return (metaData) -> {
            instance.setMetadata(metaData);
            return CompletableFuture.supplyAsync(() -> {
                try {
                    namingService.registerInstance(serviceName, instance);
                    return null;
                } catch (Exception e) {
                    throw new CompletionException(e);
                }
            }, getThreadPool(serviceHost.platform()).executor());
        };
    }

    @Override
    protected CompletableFuture<Void> doDeregister(String serviceName, ServiceHost serviceHost) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = serviceHost.url();
                namingService.deregisterInstance(serviceName, url.host(), url.port());
                return null;
            } catch (NacosException e) {
                throw new CompletionException(e);
            }
        }, getThreadPool(serviceHost.platform()).executor());

    }

    @Override
    protected CompletableFuture<List<URL>> doDiscover(String serviceName, EffiRpcModule module) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Instance> instances = namingService.selectInstances(serviceName, true);
                return instances.stream().map(this::instanceToURL).toList();
            } catch (NacosException e) {
                throw new CompletionException(e);
            }
        }, getThreadPool(module.platform()).executor());
    }

    @Override
    protected void doSubscribe(String serviceName) throws Throwable {
        namingService.subscribe(serviceName, event -> {
            if (event instanceof NamingEvent namingEvent) {
                List<Instance> instances = namingEvent.getInstances();
                List<URL> healthServerUrls = instances.stream()
                        .filter(instance -> instance.isHealthy() && instance.getMetadata().containsKey(KeyConstant.PROTOCOL))
                        .map(this::instanceToURL)
                        .toList();
                onServicesUpdated(serviceName, healthServerUrls);
            }
        });
    }

    @Override
    public void doClose() throws Throwable {
        namingService.shutDown();
    }

    private URL instanceToURL(Instance instance) {
        String protocol = instance.getMetadata().get(KeyConstant.PROTOCOL).toLowerCase();
        return URL.builder()
                .protocol(protocol)
                .address(NetUtil.toAddress(instance.getIp(), instance.getPort()))
                .params(instance.getMetadata())
                .build();
    }
}
