package io.effi.rpc.processor;

import io.effi.rpc.nativetools.ProxyConfig;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Handles proxy configuration for native-image.
 */
public class ProxyConfigResourceSection extends NativeConfigResourceSection<ProxyConfig> {

    public ProxyConfigResourceSection(ProcessingEnvironment processingEnv) {
        super(processingEnv, new ProxyConfig());
    }

}
