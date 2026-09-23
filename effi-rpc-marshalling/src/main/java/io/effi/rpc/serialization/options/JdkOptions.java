package io.effi.rpc.serialization.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.STRING_ARRAY;

/**
 * Defines JDK serialization options.
 */
public interface JdkOptions {

    String[] DEFAULT_ALLOWED_PACKAGES = {
            "io.effi.rpc.",
            "java.lang.",
            "java.util.",
            "java.time."
    };

    OptionName<String[]> ALLOWED_PACKAGES = STRING_ARRAY.currentFirst("serialization.jdk.allowedPackages", DEFAULT_ALLOWED_PACKAGES);

    OptionName<String[]> INCLUDE_PACKAGES = STRING_ARRAY.mergeParent("serialization.jdk.includePackages");
}
