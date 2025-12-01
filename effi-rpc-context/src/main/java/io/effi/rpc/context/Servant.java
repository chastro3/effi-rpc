package io.effi.rpc.context;

import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.context.parameter.ParameterBinding;

import java.lang.reflect.Method;

import static io.effi.rpc.annotation.component.ScopedComponent.Scope.MODULE;
import static io.effi.rpc.config.OptionName.Strategy.CURRENT_FIRST;
import static io.effi.rpc.config.OptionName.Strategy.MERGE_PARENT;

/**
 * Represents an RPC servant that handle remote service invocations.
 * <p>
 * Provides the interface for receiving and processing RPC calls,
 * managing remote services, methods, and parameter mapping.
 */
@ScopedComponent(scope = MODULE)
public interface Servant extends Peer {

    OptionName<String> LABEL = OptionName.of("label", CURRENT_FIRST);

    OptionName<Integer[]> EXCLUDED_PORT = OptionName.of("excludedPort", MERGE_PARENT);

    OptionName<String[]> DECLARED_PROTOCOL = OptionName.of("protocol", CURRENT_FIRST);

    /**
     * Returns the group associated with this servant.
     */
    ServantGroup<?> group();

    /**
     * Returns the method associated with this callee.
     */
    Method method();

    /**
     * Returns the index of the method in the remote service.
     */
    int methodIndex();

    /**
     * Invokes the method with given arguments.
     *
     * @param args arguments to pass to the method
     * @return the result of the invocation
     */
    Object invoke(Object... args);

    /**
     * Returns parameter bindings for the method.
     */
    ParameterBinding[] parameterBindings();

    /**
     * Returns a description of this servant.
     */
    String label();
}



