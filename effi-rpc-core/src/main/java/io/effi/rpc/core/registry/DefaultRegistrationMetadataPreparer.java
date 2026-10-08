package io.effi.rpc.core.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.registry.RegistrationPreparer;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.registry.util.RegistryUtil;

import static io.effi.rpc.core.registry.DefaultRegistrationMetadataPreparer.NAME;

/**
 * Provides default registration metadata for service instances.
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
