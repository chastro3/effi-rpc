package io.effi.rpc.context.parameter;

import io.effi.rpc.context.invocation.Invocation;
import io.effi.rpc.context.invocation.InvocationArguments;
import io.effi.rpc.context.invocation.InvocationAttributes;
import io.effi.rpc.context.invocation.MethodInvocation;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.ReflectionUtil;

/**
 * Executes the parameter bindings declared by one method.
 */
public record MethodBinder(MethodBinding binding) {

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
        for (int i = 0; i < binding.size(); i++) {
            ParameterBinding parameter = binding.parameter(i);
            Object value = arguments[i];
            parameter.binder().bind(value, parameter, invocation);
        }
        return invocation;
    }

    public Object[] resolve(Invocation invocation) {
        Object[] arguments = new Object[binding.size()];
        for (int i = 0; i < binding.size(); i++) {
            ParameterBinding parameter = binding.parameter(i);
            Object value = parameter.binder().resolve(parameter, invocation);
            arguments[i] = ReflectionUtil.convertToParameterType(value, parameter.parameter());
        }
        return arguments;
    }
}
