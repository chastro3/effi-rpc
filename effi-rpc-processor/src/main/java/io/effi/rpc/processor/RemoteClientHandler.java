package io.effi.rpc.processor;

import io.effi.rpc.annotation.rpc.EffiRpcClient;
import io.effi.rpc.nativetools.ProxyConfig;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * Handles the {@link EffiRpcClient} annotation.
 */
public class RemoteClientHandler extends AnnotationHandler<EffiRpcClient> {

    protected RemoteClientHandler(ProcessingEnvironment processingEnv) {
        super(EffiRpcClient.class, processingEnv);
    }

    @Override
    protected void handle(Set<? extends Element> elements, RoundEnvironment roundEnv) {
        ProxyConfigResourceSection section = resourceSection(ProxyConfigResourceSection.class);
        for (Element element : elements) {
            if (element instanceof TypeElement typeElement) {
                String interfaceName = helper().qualifiedNameOf(typeElement);
                section.nativeConfig().addItem(new ProxyConfig.Item().addInterface(interfaceName));
            }
        }
    }
}
