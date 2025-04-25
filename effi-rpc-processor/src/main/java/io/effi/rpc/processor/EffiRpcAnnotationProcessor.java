package io.effi.rpc.processor;

import io.effi.rpc.spi.Extension;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.effi.rpc.constant.SystemKey.*;

/**
 * Custom annotation processor for handling Effi-RPC annotations.
 * It processes the following annotations and generates the necessary resources:
 *
 * <ul>
 *   <li>{@link Extension}</li>
 *   <li>{@link io.effi.rpc.contract.annotation.EffiRpcService}</li>
 *   <li>{@link io.effi.rpc.contract.annotation.EffiRpcClient}</li>
 * </ul>
 */

@SupportedOptions({GROUP_ID, ARTIFACT_ID, VERSION, NATIVE_BUILD})
public class EffiRpcAnnotationProcessor extends AbstractProcessor {

    private ResourceCollector resourceCollector;

    private List<AnnotationHandler<? extends Annotation>> processors;

    private Set<String> supportedAnnotation;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processors = List.of(
                new ExtensionHandler(processingEnv),
                new RemoteServiceHandler(processingEnv),
                new RemoteClientHandler(processingEnv)
        );
        resourceCollector = new ResourceCollector(processingEnv, processors);
        supportedAnnotation = processors.stream()
                .map(item -> item.type().getName())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (AnnotationHandler<? extends Annotation> processor : processors) {
            processor.handle(roundEnv);
        }
        if (roundEnv.processingOver()) {
            try {
                resourceCollector.write();
            } catch (IOException e) {
                processingEnv.getMessager().printError(e.getMessage());
            }
        }
        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotation;
    }
}
