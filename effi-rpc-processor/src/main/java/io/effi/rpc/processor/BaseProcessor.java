package io.effi.rpc.processor;

import io.effi.rpc.common.compile.CompileTimeHelper;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

public abstract class BaseProcessor<T extends Annotation> {

    protected Class<T> annotationType;

    protected ProcessingEnvironment processingEnv;

    protected CompileTimeHelper helper;

    protected Messager messager;

    protected ResourceCollector resourceCollector;

    protected BaseProcessor(Class<T> annotationType, ResourceCollector resourceCollector) {
        this.annotationType = annotationType;
        this.resourceCollector = resourceCollector;
        this.processingEnv = resourceCollector.processingEnv();
        this.helper = resourceCollector.helper();
        this.messager = processingEnv.getMessager();
    }

    public void process(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annotationType);
        process(elements, roundEnv);
    }

    protected abstract void process(Set<? extends Element> elements, RoundEnvironment roundEnv);

    public void complete() {

    }

    public Class<T> type() {
        return annotationType;
    }

}
