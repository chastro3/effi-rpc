package io.effi.rpc.registry;

import io.effi.rpc.base.ServiceHost;
import io.effi.rpc.config.registry.RegistryConfig;
import io.effi.rpc.internal.logging.Logger;
import io.effi.rpc.internal.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Registers metadata for a given URL using registered metadata handlers.
 * <p>
 * Processes the URL and executes the associated task.
 * </p>
 */
public class RegisterTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterTask.class);

    private final Collection<MetaDataRegister> metaDataRegisters;

    private final RegistryConfig config;

    private final ServiceHost serviceHost;

    private final RegistryService.RegistrationAction registrationAction;

    private final String serviceName;

    public RegisterTask(RegistryConfig config, String serviceName, ServiceHost serviceHost,
                        RegistryService.RegistrationAction registrationAction) {
        this.config = AssertUtil.notNull(config, "config");
        this.serviceName = AssertUtil.notBlank(serviceName, "serviceName");
        this.serviceHost = AssertUtil.notNull(serviceHost, "serviceHost");
        this.registrationAction = AssertUtil.notNull(registrationAction, "registrationAction");
        this.metaDataRegisters = serviceHost.platform()
                .getApplication(serviceName)
                .extensionsOf(MetaDataRegister.class);
    }

    public CompletableFuture<Void> execute() {
        Map<String, String> metaData = new HashMap<>();
        for (MetaDataRegister metaDataRegister : metaDataRegisters) {
            metaDataRegister.process(serviceHost, metaData);
        }
        return registrationAction.execute(metaData);
    }

    @Override
    public void run() {
        execute().exceptionally(ex -> {
            logger.warn("Failed to periodically register instance '{}' for service '{}' at '{}'", ex, info());
            return null;
        });
    }

    private Object[] info() {
        return new Object[]{
                serviceHost.id(),
                serviceName,
                config.url().authority()};
    }
}

