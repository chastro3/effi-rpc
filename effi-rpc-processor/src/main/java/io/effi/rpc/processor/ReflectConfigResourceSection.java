package io.effi.rpc.processor;

import io.effi.rpc.nativetools.ReflectConfig;

import javax.annotation.processing.ProcessingEnvironment;

/**
 * Handles reflect configuration for native-image.
 */
public class ReflectConfigResourceSection extends NativeConfigResourceSection<ReflectConfig> {

    public ReflectConfigResourceSection(ProcessingEnvironment processingEnv) {
        super(processingEnv, new ReflectConfig());
    }
}
