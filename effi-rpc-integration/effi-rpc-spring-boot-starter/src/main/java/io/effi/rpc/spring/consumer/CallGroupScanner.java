package io.effi.rpc.spring.consumer;

import io.effi.rpc.annotation.rpc.CallGroup;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides scanning of application packages for interfaces annotated with {@link CallGroup}.
 */
final class CallGroupScanner {

    private CallGroupScanner() {
    }

    static List<Class<?>> scan(ListableBeanFactory beanFactory) {
        List<String> packages;
        try {
            packages = AutoConfigurationPackages.get(beanFactory);
        } catch (IllegalStateException ignored) {
            return List.of();
        }
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false) {
                    @Override
                    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                        return beanDefinition.getMetadata().isInterface();
                    }
                };
        scanner.addIncludeFilter(new AnnotationTypeFilter(CallGroup.class));
        ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
        List<Class<?>> consumers = new ArrayList<>();
        for (String scanPackage : packages) {
            scanner.findCandidateComponents(scanPackage).forEach(candidate -> consumers.add(
                    ClassUtils.resolveClassName(candidate.getBeanClassName(), classLoader)
            ));
        }
        return consumers;
    }
}
