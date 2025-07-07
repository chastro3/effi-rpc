package io.effi.rpc.base.context;

import io.effi.rpc.annotation.component.ScopedComponent;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;

/**
 * Defines a chain of {@link Stage} execution units.
 */
@ScopedComponent(scope = MODULE)
public interface StageChain extends ExecutionUnitChain {}