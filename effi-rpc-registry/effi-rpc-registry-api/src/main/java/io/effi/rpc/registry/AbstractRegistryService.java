package io.effi.rpc.registry;

import io.effi.rpc.common.constant.KeyConstant;
import io.effi.rpc.common.exception.EffiRpcException;
import io.effi.rpc.common.exception.PredefinedErrorCode;
import io.effi.rpc.common.url.URL;
import io.effi.rpc.common.util.AssertUtil;
import io.effi.rpc.common.util.CollectionUtil;
import io.effi.rpc.common.util.StringUtil;
import io.effi.rpc.contract.module.EffRpcApplication;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Abstract implementation of {@link RegistryService}.
 */
public abstract class AbstractRegistryService implements RegistryService {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected final Map<String, List<String>> discoverHealthServices = new ConcurrentHashMap<>();

    protected final Map<String, URL> registeredUrls = new ConcurrentHashMap<>();

    protected final EffRpcApplication application;

    protected URL registryUrl;

    protected boolean enableHealthCheck;

    protected AbstractRegistryService(EffRpcApplication application, URL url) {
        this.application = AssertUtil.notNull(application, "application");
        this.registryUrl = url;
        this.enableHealthCheck = url.getBooleanParam(KeyConstant.ENABLE_HEALTH_CHECK, true);
        connect(url);
    }

    @Override
    public void register(URL url) {
        if (!registeredUrls.containsKey(url.authority())) {
            String serviceName = serviceName(url);
            var task = buildRegisterTask(serviceName, url);
            try {
                RegisterTask registerTask = new RegisterTask(url, task);
                logger.info("Registered service(s) for '{}' in registry at '{}'", serviceName, registryUrl.address());
            } catch (Exception e) {
                throw EffiRpcException.wrap(PredefinedErrorCode.REGISTRY_REGISTER, e,
                        serviceName(url), registryUrl.address()
                );
            }
//            EffiRpcBootstrap.staringInstance()
//                    .moduleCentral().scheduler().addPeriodic(registerTask, 5, 5, TimeUnit.SECONDS);
//            logger.info("The <{}>{} service register is executed every 5s", url.protocol(), url.address());
            registeredUrls.put(url.authority(), url);
        }
    }

    @Override
    public List<URL> discover(URL url) {
        String serviceName = serviceName(url);
        List<URL> urls;
        // Check the cache for available services
        List<String> serverUrls = discoverHealthServices.get(serviceName);
        if (CollectionUtil.isEmpty(serverUrls)) {
            synchronized (serviceName) {
                serverUrls = discoverHealthServices.get(serviceName);
                if (CollectionUtil.isEmpty(serverUrls)) {
                    boolean noSubscribe = serverUrls == null;
                    // Discover available services from the registry
                    try {
                        urls = doDiscover(serviceName, url);
                        logger.info("Discovered {} service(s) for '{}' in registry at '{}'", urls.size(), serviceName, registryUrl.address());
                    } catch (Throwable e) {
                        throw EffiRpcException.wrap(
                                PredefinedErrorCode.REGISTRY_DISCOVER, e,
                                serviceName, registryUrl.address()
                        );
                    }
                    serverUrls = urls.stream().map(URL::toString).toList();
                    // Subscribe to services if needed
                    if (noSubscribe) {
                        try {
                            doSubscribe(serviceName, url);
                            logger.info("Subscribed service(s) for '{}' in registry at '{}'", serviceName, registryUrl.address());
                        } catch (Throwable e) {
                            throw EffiRpcException.wrap(
                                    PredefinedErrorCode.REGISTRY_SUBSCRIBE, e,
                                    serviceName, registryUrl.address()
                            );
                        }
                    }
                    discoverHealthServices.put(serviceName, serverUrls);
                }
            }
        }
        urls = serverUrls.stream()
                .map(URL::valueOf)
                .filter(serverUrl -> serverUrl.protocol().equalsIgnoreCase(url.protocol()))
                .toList();

        return urls;
    }

    @Override
    public void deregister(URL exporterUrl) {
        String serviceName = serviceName(exporterUrl);
        try {
            doDeregister(serviceName, exporterUrl);
            logger.info("Deregistered service(s) for '{}' in registry at '{}'", serviceName, registryUrl.address());
        } catch (Throwable e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.REGISTRY_DEREGISTER, e,
                    serviceName, registryUrl.address()
            );
        }
    }

    @Override
    public void close() {
        registeredUrls.values().forEach(this::deregister);
        discoverHealthServices.clear();
        registeredUrls.clear();
        try {
            doClose();
        } catch (Throwable e) {
            throw EffiRpcException.wrap(PredefinedErrorCode.CLOSE, e, this);
        }
    }

    protected String instanceId(URL url) {
        return "<" + url.protocol() + ">" + url.address();
    }

    protected String serviceName(URL url) {
        String serviceName = EffRpcApplication.getName(url);
        if (StringUtil.isBlank(serviceName)) throw new IllegalArgumentException("service name cannot be blank");
        return serviceName;
    }

    protected abstract BiConsumer<RegisterTask, Map<String, String>> buildRegisterTask(String serviceName, URL url);

    protected abstract void doSubscribe(String serviceName, URL url) throws Throwable;

    protected abstract void doDeregister(String serviceName, URL url) throws Throwable;

    protected abstract List<URL> doDiscover(String serviceName, URL url) throws Throwable;

    protected abstract void doClose() throws Throwable;
}

