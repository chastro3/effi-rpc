package io.effi.rpc.processor;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.annotation.component.ScopedComponent;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Handles the {@link Extensible} annotation.
 */
public class ExtensibleHandler extends AnnotationHandler<Extensible> {

    protected ExtensibleHandler(ProcessingEnvironment processingEnv) {
        super(Extensible.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        ScopedComponentResourceSection resourceSection = resourceSection(ScopedComponentResourceSection.class);
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                Extensible extensible = typeElement.getAnnotation(Extensible.class);
                String typeName = helper().qualifiedNameOf(typeElement);
                resourceSection.add(typeName, extensible.scope().name(), ScopedComponent.Kind.MULTI.name());
            }
        }
    }
}
