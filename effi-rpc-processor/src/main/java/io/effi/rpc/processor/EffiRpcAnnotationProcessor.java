package io.effi.rpc.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.effi.rpc.common.constant.SystemKey.*;

@SupportedOptions({GROUP_ID, ARTIFACT_ID, VERSION, NATIVE_BUILD})
public class EffiRpcAnnotationProcessor extends AbstractProcessor {

    private ResourceCollector resourceCollector;

    private List<BaseProcessor<? extends Annotation>> processors;

    private Set<String> supportedAnnotation;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        resourceCollector = new ResourceCollector(processingEnv);
        processors = List.of(
                new ExtensionProcessor(resourceCollector),
                new RemoteServiceProcessor(resourceCollector),
                new RemoteClientProcessor(resourceCollector)
        );
        supportedAnnotation = processors.stream()
                .map(item -> item.type().getName())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (BaseProcessor<? extends Annotation> processor : processors) {
            processor.process(roundEnv);
        }
        if (roundEnv.processingOver()) resourceCollector.generateFiles();
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
