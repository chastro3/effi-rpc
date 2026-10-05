package io.effi.rpc.registry;

import io.effi.rpc.concurrent.Future;
import io.effi.rpc.logging.Logger;
import io.effi.rpc.logging.LoggerFactory;
import io.effi.rpc.util.AssertUtil;

import java.util.Collection;

/**
 * Registers service instances through the configured preparers and registration action.
 */
public class RegisterTask implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RegisterTask.class);

    private final RegistryClient client;

    private final ServiceInstance instance;

    private final RegistryClient.Registration registration;

    private final Collection<RegistrationPreparer> preparers;

    public RegisterTask(RegistryClient client, ServiceInstance instance, RegistryClient.Registration registration) {
        this.client = AssertUtil.notNull(client, "client");
        this.instance = AssertUtil.notNull(instance, "instance");
        this.registration = AssertUtil.notNull(registration, "registrationAction");
        this.preparers = client.platform().extensions(RegistrationPreparer.class);
    }

    /**
     * Runs the configured preparers and returns the registration future.
     */
    public Future<Void> execute() {
        preparers.forEach(p -> p.prepare(instance));
        return registration.register(instance);
    }

    @Override
    public void run() {
        execute().onComplete(res -> {
            if (res.failed()) {
                logger.warn(
                        "Failed to periodically register instance '{}' for service '{}' at '{}'", res.cause(),
                        instance.id(), instance.serviceName(), client
                );
            }
        });
    }

}

