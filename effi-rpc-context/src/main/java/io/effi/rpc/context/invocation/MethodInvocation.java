package io.effi.rpc.context.invocation;

import io.effi.rpc.util.AssertUtil;

/**
 * Represents a method invocation with signature metadata.
 */
public final class MethodInvocation extends PositionalInvocation {

    private final MethodSignature signature;

    public MethodInvocation(MethodSignature signature, InvocationArguments arguments) {
        this(signature, arguments, new InvocationAttributes());
    }

    public MethodInvocation(MethodSignature signature, InvocationArguments arguments, InvocationAttributes attributes) {
        super(arguments, attributes);
        this.signature = AssertUtil.notNull(signature, "signature");
    }

    public MethodSignature signature() {
        return signature;
    }
}
