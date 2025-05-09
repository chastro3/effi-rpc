package io.effi.rpc.processor;

import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.SystemKey;
import io.effi.rpc.nativetools.JsonWriter;
import io.effi.rpc.nativetools.NativeConfig;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
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
        this.modulePath = getModulePath();
    }

    @Override
    public void write() throws IOException {
        if (nativeConfig.hasResource()) {
            String filePath = Constant.NATIVE_IMAGE_PREFIX + modulePath + "generated/" + nativeConfig.name();
            FileObject resource = filer().createResource(StandardLocation.CLASS_OUTPUT, "", filePath);
            try (JsonWriter jsonWriter = new JsonWriter(resource.openWriter())) {
                jsonWriter.write(nativeConfig.toJsonConfig());
            }
        }
    }

    public T nativeConfig() {
        return nativeConfig;
    }

    private String getModulePath() {
        String groupId = processingEnv().getOptions().get(SystemKey.GROUP_ID);
        String artifactId = processingEnv().getOptions().get(SystemKey.ARTIFACT_ID);
        if (groupId == null || artifactId == null) {
            return "";
        }
        return groupId + "/" + artifactId + "/";
    }
}
