package io.effi.rpc.nativetools;

/**
 * Provides native operations.
 */
public class NativeUtil {

    private static final boolean IMAGE_CODE = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    public static boolean inNativeImage() {
        return IMAGE_CODE;
    }

}
