package io.effi.rpc.util;

import io.effi.rpc.constant.SystemKeys;

/**
 * Parses the current Java version and exposes it as an integer constant.
 * Supports both legacy (1.x) and modern (x) formats.
 */
public final class JavaVersion {

    private static final int CURRENT;

    static {
        String v = System.getProperty(SystemKeys.JAVA_VERSION);
        int major;
        if (v.startsWith("1.")) {
            major = v.charAt(2) - '0';
        } else {
            int i = 0;
            int n = 0;
            while (i < v.length()) {
                char c = v.charAt(i++);
                if (c >= '0' && c <= '9') {
                    n = n * 10 + (c - '0');
                } else {
                    break;
                }
            }
            major = n;
        }
        CURRENT = major;
    }

    public static int get() {
        return CURRENT;
    }

}
