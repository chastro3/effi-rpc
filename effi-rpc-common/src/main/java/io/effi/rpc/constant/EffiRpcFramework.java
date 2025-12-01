package io.effi.rpc.constant;

import io.effi.rpc.util.ClassUtil;
import io.effi.rpc.util.Messages;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class EffiRpcFramework {

    private static final String VERSION = loadVersion();

    private static final int JAVA_VERSION = findJavaVersion();

    public static String version() {
        return VERSION;
    }

    public static int javaVersion() {
        return JAVA_VERSION;
    }

    private static String loadVersion() {
        String path = ResourcePaths.VERSION_FILE;
        try {
            InputStream stream = ClassUtil.findClassLoader(EffiRpcFramework.class)
                    .getResourceAsStream(path);
            if (stream == null) {
                throw new IllegalStateException("Cannot find resource file: " + path);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
                return reader.readLine();
            }
        } catch (Exception e) {
            throw new IllegalStateException(Messages.parseFile(path), e);
        }
    }

    private static int findJavaVersion() {
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
        return major;
    }
}
