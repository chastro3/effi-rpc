package io.effi.rpc.processor;

import io.effi.rpc.constant.ResourcePaths;
import io.effi.rpc.constant.SystemKeys;
import io.effi.rpc.nativetools.JsonWriter;
import io.effi.rpc.nativetools.NativeConfig;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import java.io.IOException;

/**
 * Collects native-image configuration and generates the corresponding files.
 */
public class NativeConfigResourceSection<T extends NativeConfig<?>> extends Helper implements ResourceSection {

    private final T nativeConfig;

    private final String modulePath;

    protected NativeConfigResourceSection(ProcessingEnvironment processingEnv, T nativeConfig) {
        super(processingEnv);
        this.nativeConfig = nativeConfig;
        this.modulePath = findModulePath();
    }

    @Override
    public void write() throws IOException {
        if (nativeConfig.hasResource()) {
            String filePath = ResourcePaths.NATIVE_IMAGE_DIR + modulePath + "generated/" + nativeConfig.name();
            FileObject resource = helper().createOutputFile(filePath);
            try (JsonWriter jsonWriter = new JsonWriter(resource.openWriter())) {
                jsonWriter.write(nativeConfig.toJsonConfig());
            }
        }
    }

    public T nativeConfig() {
        return nativeConfig;
    }

    private String findModulePath() {
        String groupId = processingEnv().getOptions().get(SystemKeys.GROUP_ID);
        String artifactId = processingEnv().getOptions().get(SystemKeys.ARTIFACT_ID);
        if (groupId == null || artifactId == null) {
            return "";
        }
        return groupId + "/" + artifactId + "/";
    }
}
