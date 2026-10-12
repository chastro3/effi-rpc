package io.effi.rpc.context.invocation;

import io.effi.rpc.util.AssertUtil;

import java.lang.reflect.Method;

/**
 * Defines a method invocation backed by a reflected method.
 */
public final class MethodInvocation extends PositionalInvocation {

    private final Method method;

    private final MethodSignature signature;

    public MethodInvocation(Method method, MethodSignature signature, InvocationArguments arguments) {
        this(method, signature, arguments, new InvocationAttributes());
    }

    public MethodInvocation(
            Method method,
            MethodSignature signature,
            InvocationArguments arguments,
            InvocationAttributes attributes
    ) {
        super(arguments, attributes);
        this.method = AssertUtil.notNull(method, "method");
        this.signature = AssertUtil.notNull(signature, "signature");
    }

    /**
     * Returns the invoked method.
     */
    public Method method() {
        return method;
    }

    /**
     * Returns the invoked method signature.
     */
    public MethodSignature signature() {
        return signature;
    }
}
