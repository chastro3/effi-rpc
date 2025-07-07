package io.effi.rpc.base.context;

import io.effi.rpc.annotation.component.ScopedComponent;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Defines a chain of {@link Interceptor} execution units.
 */
@ScopedComponent(scope = MODULE)
public interface InterceptorChain extends ExecutionUnitChain {}
