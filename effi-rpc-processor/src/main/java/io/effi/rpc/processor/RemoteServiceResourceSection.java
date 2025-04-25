package io.effi.rpc.processor;

import io.effi.rpc.compile.DynamicAccessorGenerator;
import io.effi.rpc.compile.GeneratedInfo;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Collects remote service types and generates corresponding class files.
 */
public class RemoteServiceResourceSection extends Helper implements ResourceSection {

    private final List<TypeElement> remoteServices = new ArrayList<>();

    public RemoteServiceResourceSection(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    public void add(TypeElement type) {
        remoteServices.add(type);
    }

    @Override
    public void write() throws IOException {
        for (TypeElement type : remoteServices) {
            GeneratedInfo generatedInfo = DynamicAccessorGenerator.fromTypeElement(type, helper());
            FileObject fo = filer().createResource(StandardLocation.CLASS_OUTPUT, generatedInfo.pkg(), generatedInfo.name() + ".class", type);
            try (OutputStream os = fo.openOutputStream()) {
                os.write(generatedInfo.data());
            }
        }
    }
}
