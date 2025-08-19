package io.effi.rpc.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.effi.rpc.async.Future;
import io.effi.rpc.component.ScopedPlatform;
import io.effi.rpc.component.registry.RegistryConfig;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.constant.KeyConstant;
import io.effi.rpc.registry.AbstractRegistryClient;
import io.effi.rpc.registry.DefaultServiceInstance;
import io.effi.rpc.registry.RegistryClient;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.util.StringUtil;

import java.util.List;
import java.util.Map;

/**
 * Implements {@link RegistryClient} using Nacos.
 * <p>
 * See <a href="https://github.com/alibaba/nacos">Nacos</a> for details.
 */
public class NacosRegistryClient extends AbstractRegistryClient {

    private final NamingService namingService;

    protected NacosRegistryClient(RegistryConfig config, ScopedPlatform platform) {
        super(config, platform, true);
        this.namingService = createNamingService(config);
    }

    @Override
    public boolean isActive() {
        return namingService.getServerStatus().equals("UP");
    }

    @Override
    public Registration createRegistrationAction(ServiceInstance instance) {
        String instanceId = instance.id();
        Instance inst = new Instance();
        inst.setInstanceId(instanceId);
        inst.setIp(instance.host());
        inst.setPort(instance.port());
        return (serviceInst) -> {
            inst.setMetadata(serviceInst.metadata());
            return threadPool.execute(() -> {
                try {
                    namingService.registerInstance(instance.serviceName(), inst);
                } catch (Exception e) {
                    throw NacosErrorCodes.REGISTER_INSTANCE.fail(e);
                }
            });
        };
    }

    @Override
    protected Future<Void> doDeregister(ServiceInstance instance) {
        return threadPool.execute(() -> {
            try {
                namingService.deregisterInstance(instance.serviceName(), instance.host(), instance.port());
            } catch (NacosException e) {
                throw NacosErrorCodes.DEREGISTER_INSTANCE.fail(e);
            }
        });
    }

    @Override
    protected Future<List<ServiceInstance>> doLookup(String serviceName) {
        return threadPool.execute(() -> {
            try {
                List<Instance> instances = namingService.selectInstances(serviceName, true);
                return instances.stream().map(this::toServiceInstance).toList();
            } catch (NacosException e) {
                throw NacosErrorCodes.LOOKUP_INSTANCE.fail(e);
            }
        });
    }

    @Override
    protected void doSubscribe(String serviceName) throws Throwable {
        namingService.subscribe(serviceName, event -> {
            if (event instanceof NamingEvent namingEvent) {
                List<Instance> instances = namingEvent.getInstances();
                List<ServiceInstance> healthServerInstances = instances.stream()
                        .filter(instance -> instance.isHealthy() && instance.getMetadata().containsKey(KeyConstant.PROTOCOL))
                        .map(this::toServiceInstance)
                        .toList();
                onServicesUpdated(serviceName, healthServerInstances);
            }
        });
    }

    @Override
    public void doClose() throws Throwable {
        namingService.shutDown();
    }

    private NamingService createNamingService(RegistryConfig config) {
        String projectName = config.getConfig(ConfigNames.NACOS_PROJECT_NAME);
        if (StringUtil.isNotBlank(projectName))
            // nacos <project.name>
            System.setProperty("project.name", projectName);
        try {
            return NamingFactory.createNamingService(config.address());
        } catch (NacosException e) {
            throw NacosErrorCodes.NAMING_SERVICE_CREATE.fail(e);
        }
    }

    private ServiceInstance toServiceInstance(Instance instance) {
        Map<String, String> metadata = instance.getMetadata();
        String protocol = metadata.get(KeyConstant.PROTOCOL).toLowerCase();
        return DefaultServiceInstance.builder()
                .id(instance.getInstanceId())
                .protocol(protocol)
                .serviceName(instance.getServiceName())
                .host(instance.getIp())
                .port(instance.getPort())
                .addMetadata(metadata)
                .build();
    }
}
