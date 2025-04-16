package io.effi.rpc.nativetools;

public interface NativeConfig<T> {

    String name();

    T toJsonConfig();

    boolean hasResource();
}
