package io.effi.rpc.nativetools;

public class NativeUtil {

    private static final boolean IMAGE_CODE = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

    private static final Runtime.Version CURRENT_VERSION = Runtime.version();

    public static boolean inNativeImage() {
        return IMAGE_CODE;
    }

    public static boolean isGraalVMVersionAtLeast(String targetVersion) {
        return CURRENT_VERSION.compareTo(Runtime.Version.parse(targetVersion)) > 0;
    }

}
