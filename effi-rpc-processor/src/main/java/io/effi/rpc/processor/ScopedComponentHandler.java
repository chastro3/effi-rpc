package io.effi.rpc.processor;

import io.effi.rpc.annotation.component.ScopedComponent;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Handles the {@link ScopedComponent} annotation.
 */
public class ScopedComponentHandler extends AnnotationHandler<ScopedComponent> {

    public ScopedComponentHandler(ProcessingEnvironment processingEnv) {
        super(ScopedComponent.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        ScopedComponentResourceSection resourceSection = resourceSection(ScopedComponentResourceSection.class);
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String type = helper().qualifiedNameOf(typeElement);
                ScopedComponent component = typeElement.getAnnotation(ScopedComponent.class);
                resourceSection.add(type, component.scope().name(), component.kind().name());
            }
        }
    }

}
