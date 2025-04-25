package io.effi.rpc.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Abstract class for handling annotation.
 *
 * @param <T> The annotation type this handler processes.
 */
public abstract class AnnotationHandler<T extends Annotation> extends Helper {

    protected Class<T> annotationType;

    protected ResourceCollector resourceCollector;

    protected AnnotationHandler(Class<T> annotationType, ProcessingEnvironment processingEnv) {
        super(processingEnv);
        this.annotationType = annotationType;
    }

    public void handle(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationType);
        handle(elements, roundEnv);
    }

    void setResourceCollector(ResourceCollector resourceCollector) {
        this.resourceCollector = resourceCollector;
    }

    protected abstract void handle(Set<? extends Element> elements, RoundEnvironment roundEnv);

    protected <R extends ResourceSection> R getResourceSection(Class<R> type) {
        return resourceCollector.section(type);
    }

    public Class<T> type() {
        return annotationType;
    }

}
