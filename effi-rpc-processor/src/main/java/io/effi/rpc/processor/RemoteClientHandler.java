package io.effi.rpc.processor;

import io.effi.rpc.annotation.rpc.EffiRpcClient;
import io.effi.rpc.nativetools.ProxyConfigItem;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Handles the processing of the {@link EffiRpcClient} annotation.
 */
public class RemoteClientHandler extends AnnotationHandler<EffiRpcClient> {

    protected RemoteClientHandler(ProcessingEnvironment processingEnv) {
        super(EffiRpcClient.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        ProxyConfigResourceSection section = getResourceSection(ProxyConfigResourceSection.class);
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String interfaceName = helper().getQualifiedClassName(typeElement);
                section.nativeConfig().addItem(new ProxyConfigItem().addInterface(interfaceName));
            }
        }
    }
}
