package io.effi.rpc.processor;

import io.effi.rpc.common.compile.CompileTimeHelper;
import io.effi.rpc.common.compile.DynamicAccessorGenerator;
import io.effi.rpc.common.compile.GeneratedInfo;
import io.effi.rpc.common.constant.Constant;
import io.effi.rpc.common.constant.SystemKey;
import io.effi.rpc.nativetools.*;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ResourceCollector {

    private final Map<String, Set<String>> extensionEntries = new HashMap<>();

    private final List<TypeElement> remoteServices = new ArrayList<>();

    private final ReflectConfig reflectConfig = new ReflectConfig();

    private final ProxyConfig proxyConfig = new ProxyConfig();

    private final ProcessingEnvironment processingEnv;

    private final CompileTimeHelper helper;

    private final Messager messager;

    private final Filer filer;

    private final boolean isNativeBuild;

    private final String modulePath;

    public ResourceCollector(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.helper = new CompileTimeHelper(processingEnv);
        this.messager = processingEnv.getMessager();
        this.filer = processingEnv.getFiler();
        this.modulePath = getModulePath();
        this.isNativeBuild = "true".equals(processingEnv.getOptions().getOrDefault(SystemKey.NATIVE_BUILD, "false"));
    }

    public void addExtension(String interfaceName, String extensionName) {
        Set<String> services = extensionEntries.computeIfAbsent(interfaceName, k -> new HashSet<>());
        services.add(extensionName);
        if (isNativeBuild) {
            ReflectConfigItem reflectConfigItem = new ReflectConfigItem()
                    .condition(new ConditionItem().typeReachable(interfaceName))
                    .name(extensionName)
                    .method("<init>", null);
            addReflectConfigItem(reflectConfigItem);
        }
    }

    public void addRemoteService(TypeElement type) {
        remoteServices.add(type);
    }

    public void addReflectConfigItem(ReflectConfigItem item) {
        reflectConfig.addItem(item);
    }

    public void addProxyInterface(ProxyConfigItem item) {
        proxyConfig.addItem(item);
    }

    public void generateFiles() {
        generateExtensionFile();
        generateDynamicAccessorFile();
        if (isNativeBuild) {
            generateNativeConfigFile(reflectConfig);
            generateNativeConfigFile(proxyConfig);
        }
    }

    ProcessingEnvironment processingEnv() {
        return processingEnv;
    }

    CompileTimeHelper helper() {
        return helper;
    }

    private void generateExtensionFile() {
        for (Map.Entry<String, Set<String>> entry : extensionEntries.entrySet()) {
            String path = Constant.SPI_FIX_PATH + entry.getKey();
            Set<String> merged = new LinkedHashSet<>(entry.getValue());
            FileObject resource;
            try {
                FileObject existing = filer.getResource(StandardLocation.CLASS_OUTPUT, "", path);
                try (InputStream in = existing.openInputStream()) {
                    merged.addAll(readServiceFile(in));
                }
            } catch (IOException ignored) {
            }
            try {
                resource = filer.createResource(StandardLocation.CLASS_OUTPUT, "", path);
                try (OutputStream out = resource.openOutputStream()) {
                    writeServiceFile(merged, out);
                }
            } catch (IOException e) {
                processingEnv.getMessager().printError("Failed to write extension file " + path + ": " + e.getMessage());
            }
        }
    }

    private void generateDynamicAccessorFile() {
        for (TypeElement type : remoteServices) {
            try {
                GeneratedInfo generatedInfo = DynamicAccessorGenerator.fromTypeElement(type, helper);
                FileObject fo = helper.processingEnv()
                        .getFiler()
                        .createResource(StandardLocation.CLASS_OUTPUT, generatedInfo.pkg(), generatedInfo.name() + ".class", type);
                try (OutputStream os = fo.openOutputStream()) {
                    os.write(generatedInfo.data());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void generateNativeConfigFile(NativeConfig<?> nativeConfig) {
        if (nativeConfig.hasResource()) {
            try (Writer writer = createNativeFile(nativeConfig.name()).openWriter()) {
                JsonWriter jsonWriter = new JsonWriter(writer);
                jsonWriter.write(nativeConfig.toJsonConfig());
            } catch (IOException e) {
                messager.printError(e.getMessage());
            }
        }
    }

    private FileObject createNativeFile(String fileName) {
        try {
            String filePath = Constant.NATIVE_IMAGE_PREFIX + modulePath + "generated/" + fileName;
            return filer.createResource(StandardLocation.CLASS_OUTPUT, "", filePath);
        } catch (IOException e) {
            messager.printError(e.getMessage());
            throw new IllegalStateException(e);
        }
    }

    private String getModulePath() {
        String groupId = processingEnv.getOptions().get(SystemKey.GROUP_ID);
        String artifactId = processingEnv.getOptions().get(SystemKey.ARTIFACT_ID);
        if (groupId == null || artifactId == null) {
            return "";
        }
        return groupId + "/" + artifactId + "/";
    }

    private Set<String> readServiceFile(InputStream input) throws IOException {
        HashSet<String> serviceClasses = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int commentStart = line.indexOf('#');
                if (commentStart >= 0) {
                    line = line.substring(0, commentStart);
                }
                line = line.trim();
                if (!line.isEmpty()) {
                    serviceClasses.add(line);
                }
            }
            return serviceClasses;
        }
    }

    private void writeServiceFile(Collection<String> services, OutputStream output) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, UTF_8));
        for (String service : services) {
            writer.write(service);
            writer.newLine();
        }
        writer.flush();
    }

}
