package io.effi.rpc.boot.registry;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.component.ScopedApplication;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.registry.RegistrationPreparer;
import io.effi.rpc.registry.ServiceInstance;
import io.effi.rpc.registry.util.RegistryUtil;

import static io.effi.rpc.boot.registry.DefaultRegistrationPreparer.NAME;

/**
 * Register default meta data to registry.
 */
@Extension(NAME)
public class DefaultRegistrationPreparer implements RegistrationPreparer {

    public static final String NAME = Constant.DEFAULT_NAME;

    @Override
    public void prepare(ServiceInstance instance) {
        ScopedApplication application = RegistryUtil.lookupApplication(instance);
        if (application != null) {
            DefaultRegistryMetaData defaultRegistryMetaData = new DefaultRegistryMetaData(application);
            instance.addMetadata(defaultRegistryMetaData.toMap());
        }
    }
}
