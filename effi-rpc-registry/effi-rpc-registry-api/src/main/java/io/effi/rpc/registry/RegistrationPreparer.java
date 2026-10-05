package io.effi.rpc.registry;

import io.effi.rpc.annotation.component.Extensible;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.PLATFORM;

/**
 * Prepares a service instance before it is registered.
 */
@Extensible(lazyLoad = false, scope = PLATFORM)
public interface RegistrationPreparer {

    /**
     * Prepares the given service instance before registration.
     *
     * @param instance the service instance to prepare
     */
    void prepare(ServiceInstance instance);
}
