package io.effi.rpc.context.parameter;

/**
 * Combines caller-side writing and servant-side resolution for one parameter.
 */
public interface ParameterBinder extends ParameterWriter, ParameterResolver {
}
