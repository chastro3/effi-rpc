package io.effi.rpc.context.invocation;

import io.effi.rpc.util.AssertUtil;

/**
 * Defines a direct invocation without method signature metadata.
 */
public class PositionalInvocation implements Invocation {

    private final InvocationArguments arguments;

    private final InvocationAttributes attributes;

    public PositionalInvocation(Object[] arguments) {
        this(new InvocationArguments(arguments), new InvocationAttributes());
    }

    public PositionalInvocation(InvocationArguments arguments, InvocationAttributes attributes) {
        this.arguments = AssertUtil.notNull(arguments, "arguments");
        this.attributes = AssertUtil.notNull(attributes, "attributes");
    }

    @Override
    public InvocationArguments arguments() {
        return arguments;
    }

    @Override
    public InvocationAttributes attributes() {
        return attributes;
    }
}
