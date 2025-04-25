package io.effi.rpc.processor;

import javax.annotation.processing.ProcessingEnvironment;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Collects and manages different types of resource sections.
 */
public class ResourceCollector implements ResourceSection {

    private final Map<Class<? extends ResourceSection>, ResourceSection> sections = new HashMap<>();

    public ResourceCollector(ProcessingEnvironment processingEnv, List<AnnotationHandler<?>> annotationHandlers) {
        for (AnnotationHandler<?> annotationHandler : annotationHandlers) {
            annotationHandler.setResourceCollector(this);
        }
        sections.put(ExtensionResourceSection.class, new ExtensionResourceSection(processingEnv));
        sections.put(RemoteServiceResourceSection.class, new RemoteServiceResourceSection(processingEnv));
        sections.put(ReflectConfigResourceSection.class, new ReflectConfigResourceSection(processingEnv));
        sections.put(ProxyConfigResourceSection.class, new ProxyConfigResourceSection(processingEnv));
    }

    public <T extends ResourceSection> T section(Class<T> type) {
        return type.cast(sections.get(type));
    }

    @Override
    public void write() throws IOException {
        for (ResourceSection section : sections.values()) {
            section.write();
        }
    }
}
