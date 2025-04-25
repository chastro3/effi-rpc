package io.effi.rpc.processor;

import io.effi.rpc.constant.Constant;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Collects extension information and generates the corresponding SPI files.
 */
public class ExtensionResourceSection extends Helper implements ResourceSection {

    private final Map<String, Set<String>> extensionEntries = new HashMap<>();

    public ExtensionResourceSection(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public void write() throws IOException {
        for (Map.Entry<String, Set<String>> entry : extensionEntries.entrySet()) {
            String path = Constant.SPI_FIX_PATH + entry.getKey();
            Set<String> merged = new LinkedHashSet<>(entry.getValue());
            FileObject resource;
            try {
                FileObject existing = filer().getResource(StandardLocation.CLASS_OUTPUT, "", path);
                try (InputStream in = existing.openInputStream()) {
                    merged.addAll(readServiceFile(in));
                }
            } catch (IOException ignored) {
            }
            resource = filer().createResource(StandardLocation.CLASS_OUTPUT, "", path);
            try (OutputStream out = resource.openOutputStream()) {
                writeServiceFile(merged, out);
            }
        }
    }

    public void add(String interfaceName, String extensionName) {
        Set<String> services = extensionEntries.computeIfAbsent(interfaceName, k -> new HashSet<>());
        services.add(extensionName);
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
