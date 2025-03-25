package io.effi.rpc.registry.nacos;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.NetUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.registry.AbstractRegistryService;
import io.effi.rpc.registry.RegisterTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * {@link io.effi.rpc.registry.RegistryService} implementation based on the nacos client.
 * For more information,refer to the <a href = "https://github.com/alibaba/nacos">nacos</a>.
 */
public class NacosRegistryService extends AbstractRegistryService {

    private static volatile String NACOS_PROJECT_NAME;

    private NamingService namingService;

    protected NacosRegistryService(EffRpcApplication application, URL url) {
        super(application, url);
    }

    @Override
    public boolean isActive() {
        return namingService.getServerStatus().equals("UP");
    }

    @Override
    public void connect(URL url) {
        try {
            namingService = NamingFactory.createNamingService(url.address());
            isActive();
        } catch (NacosException e) {
            throw EffiRpcException.wrap(
                    PredefinedErrorCode.CONNECT, e,
                    url.address(), url.protocol()
            );
        }
    }

    @Override
    public BiConsumer<RegisterTask, Map<String, String>> buildRegisterTask(String serviceName, URL url) {
        if (NACOS_PROJECT_NAME == null) {
            synchronized (serviceName) {
                if (NACOS_PROJECT_NAME == null) {
                    NACOS_PROJECT_NAME = serviceName;
                    // nacos <project.name>
                    System.setProperty("project.name", serviceName);
                }
            }
        }
        return (registerTask, metaData) -> {
            Instance instance = new Instance();
            instance.setInstanceId(instanceId(url));
            instance.setIp(url.host());
            instance.setPort(url.port());
            instance.setMetadata(metaData);
            try {
                namingService.registerInstance(serviceName, instance);
            } catch (NacosException e) {
                throw EffiRpcException.wrap(
                        PredefinedErrorCode.REGISTRY_REGISTER, e,
                        serviceName, registryUrl.address()
                );
            }

        };
    }

    @Override
    protected void doDeregister(String serviceName, URL url) throws Throwable {
        namingService.deregisterInstance(serviceName, url.host(), url.port());
    }

    @Override
    protected List<URL> doDiscover(String serviceName, URL url) throws Throwable {
        ArrayList<URL> urls = new ArrayList<>();
        List<Instance> instances = namingService.selectInstances(serviceName, true);
        for (Instance instance : instances) {
            String protocol = instance.getMetadata().get(KeyConstant.PROTOCOL);
            if (!StringUtil.isBlank(protocol) && protocol.equalsIgnoreCase(url.protocol())) {
                urls.add(instanceToURL(instance));
            }
        }
        return urls;
    }

    @Override
    protected void doSubscribe(String serviceName, URL url) throws Throwable {
        namingService.subscribe(serviceName, event -> {
            if (event instanceof NamingEvent namingEvent) {
                List<Instance> instances = namingEvent.getInstances();
                List<String> healthServerUrls = instances.stream()
                        .filter(instance -> instance.isHealthy() && instance.getMetadata().containsKey(KeyConstant.PROTOCOL))
                        .map(instance -> instanceToURL(instance).toString())
                        .toList();
                discoverHealthServices.put(serviceName, healthServerUrls);
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
