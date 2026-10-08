package io.effi.rpc.context.parameter;

import io.effi.rpc.context.Peer;
import io.effi.rpc.context.Request;
import io.effi.rpc.context.invocation.InvocationArguments;
import io.effi.rpc.context.invocation.InvocationAttributes;
import io.effi.rpc.context.invocation.MethodInvocation;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ReflectionUtil;

/**
 * Executes the parameter bindings declared by one method.
 */
public record MethodBinder(MethodBinding binding) {

    /**
     * Binds the supplied values into an invocation.
     *
     * @param values positional argument values
     * @return bound method invocation
     */
    public MethodInvocation bind(Object[] values) {
        Object[] arguments = values == null ? new Object[0] : values;
        AssertUtil.valid(
                arguments.length == binding.size(),
                "argument count does not match method signature"
        );
        MethodInvocation invocation = new MethodInvocation(
                binding.signature(),
                new InvocationArguments(binding.positional() ? binding.size() : 0),
                new InvocationAttributes()
        );
        if (binding.positional()) {
            System.arraycopy(arguments, 0, invocation.arguments().values(), 0, arguments.length);
            return invocation;
        }
        for (int i = 0; i < binding.size(); i++) {
            ParameterBinding parameter = binding.parameter(i);
            Object value = arguments[i];
            parameter.writer().write(value, parameter, invocation);
        }
        return invocation;
    }

    /**
     * Resolves named parameter values from the supplied request and peer.
     *
     * @param request protocol request
     * @param peer target peer
     * @return resolved argument values
     */
    public Object[] resolve(Request request, Peer peer) {
        AssertUtil.valid(!binding.positional(), "positional binding requires decoded request values");
        Object[] arguments = new Object[binding.size()];
        for (int i = 0; i < binding.size(); i++) {
            ParameterBinding parameter = binding.parameter(i);
            Object value = parameter.resolver().resolve(parameter, request, peer);
            arguments[i] = ReflectionUtil.convertToParameterType(value, parameter.parameter());
        }
        return arguments;
    }

    /**
     * Resolves positional values from the decoded request values.
     *
     * @param values decoded positional values
     * @return converted argument values
     */
    public Object[] resolvePositional(Object[] values) {
        AssertUtil.valid(binding.positional(), "named binding cannot resolve positional values");
        AssertUtil.valid(
                values != null && values.length == binding.size(),
                "argument count does not match method signature"
        );
        Object[] arguments = new Object[binding.size()];
        for (int i = 0; i < binding.size(); i++) {
            arguments[i] = ReflectionUtil.convertToParameterType(
                    values[i],
                    binding.parameter(i).parameter()
            );
        }
        return arguments;
    }
}
