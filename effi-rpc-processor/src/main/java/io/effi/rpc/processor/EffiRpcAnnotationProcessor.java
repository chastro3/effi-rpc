package io.effi.rpc.processor;

import io.effi.rpc.annotation.component.Extensible;
import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.annotation.component.ScopedComponent;
import io.effi.rpc.annotation.rpc.CallGroup;
import io.effi.rpc.annotation.rpc.ServeGroup;
import io.effi.rpc.nativetools.NativeConfig;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static io.effi.rpc.constant.SystemKeys.ARTIFACT_ID;
import static io.effi.rpc.constant.SystemKeys.GROUP_ID;
import static io.effi.rpc.constant.SystemKeys.NATIVE_BUILD;
import static io.effi.rpc.constant.SystemKeys.VERSION;

/**
 * Processes Effi-RPC annotations.
 * It processes the following annotations and generates the necessary resources:
 *
 * <ul>
 *   <li>{@link Extensible}</li>
 *   <li>{@link Extension}</li>
 *   <li>{@link ServeGroup}</li>
 *   <li>{@link CallGroup}</li>
 *   <li>{@link ScopedComponent}</li>
 *   <li>{@link NativeConfig.Reflect}</li>
 * </ul>
 */
public class EffiRpcAnnotationProcessor extends AbstractProcessor {

    private ResourceCollector resourceCollector;

    private List<AnnotationHandler<?>> annotationHandlers;

    private Set<String> supportedAnnotation;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        annotationHandlers = List.of(
                new ExtensibleHandler(processingEnv),
                new ExtensionHandler(processingEnv),
                new ServeGroupHandler(processingEnv),
                new CallGroupHandler(processingEnv),
                new ScopedComponentHandler(processingEnv),
                new NativeReflectConfigHandler(processingEnv)
        );
        resourceCollector = new ResourceCollector(processingEnv, annotationHandlers);
        supportedAnnotation = annotationHandlers.stream()
                .map(item -> item.type().getName())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (AnnotationHandler<?> handler : annotationHandlers) {
                handler.handle(roundEnv);
            }
            if (roundEnv.processingOver()) {
                resourceCollector.write();
            }
        } catch (Throwable e) {
            processingEnv.getMessager()
                    .printMessage(Diagnostic.Kind.ERROR, stackTraceAsString(e));
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

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(GROUP_ID, ARTIFACT_ID, VERSION, NATIVE_BUILD);
    }

    private String stackTraceAsString(Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
