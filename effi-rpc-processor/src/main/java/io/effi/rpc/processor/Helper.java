package io.effi.rpc.processor;

import io.effi.rpc.compile.CompileTimeHelper;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;

/**
 * Provides utility methods to interact with the annotation processing environment.
 */
class Helper {

    private final CompileTimeHelper helper;

    private final ProcessingEnvironment processingEnv;

    protected Helper(ProcessingEnvironment processingEnv) {
        this.helper = new CompileTimeHelper(processingEnv);
        this.processingEnv = processingEnv;
    }

    protected CompileTimeHelper helper() {
        return helper;
    }

    protected ProcessingEnvironment processingEnv() {
        return processingEnv;
    }

    protected Elements elements() {
        return processingEnv.getElementUtils();
    }

    protected Filer filer() {
        return processingEnv.getFiler();
    }

    protected Messager messager() {
        return processingEnv.getMessager();
    }
}
