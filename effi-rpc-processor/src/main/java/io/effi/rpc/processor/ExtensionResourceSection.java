package io.effi.rpc.processor;

import io.effi.rpc.constant.ResourcePath;

import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.FileObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
            String path = ResourcePath.SPI_SERVICES_DIR + entry.getKey();
            Set<String> merged = new LinkedHashSet<>(entry.getValue());
            FileObject resource;
            try {
                FileObject existing = helper().findOutputFile(path);
                try (InputStream in = existing.openInputStream()) {
                    merged.addAll(readServiceFile(in));
                }
            } catch (IOException ignored) {
            }
            resource = helper().createOutputFile(path);
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
