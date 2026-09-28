package io.effi.rpc.boot.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.registry.RegistrationPreparer;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.registry.util.RegistryUtil;

import static io.effi.rpc.boot.registry.DefaultRegistrationMetadataPreparer.NAME;

/**
 * Registers default metadata to the registry.
 */
@Extension(NAME)
public class DefaultRegistrationMetadataPreparer implements RegistrationPreparer {

    public static final String NAME = Constant.DEFAULT_NAME;

    @Override
    public void prepare(ServiceInstance instance) {
        ScopedApplication application = RegistryUtil.lookupApplication(instance);
        if (application != null) {
            DefaultRegistryMetadata metadata = new DefaultRegistryMetadata(application);
            instance.addMetadata(metadata.toMap());
        }
    }
}
