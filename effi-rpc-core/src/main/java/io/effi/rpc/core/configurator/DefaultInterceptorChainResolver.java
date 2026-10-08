package io.effi.rpc.core.configurator;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.core.stage.CallInterceptorStage;
import io.effi.rpc.core.stage.ChosenInterceptorStage;
import io.effi.rpc.core.stage.ReplyInterceptorStage;
import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.constant.Constant;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.core.PeerDescriptor;
import io.effi.rpc.context.Stage;
import io.effi.rpc.core.configurator.classifier.CallInterceptorClassifier;
import io.effi.rpc.core.configurator.classifier.ChosenInterceptorClassifier;
import io.effi.rpc.core.configurator.classifier.InteractionUnitClassifier;
import io.effi.rpc.core.configurator.classifier.ReplyInterceptorClassifier;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.effi.rpc.core.configurator.DefaultInterceptorChainResolver.NAME;
import static io.effi.rpc.context.options.InterceptorOptions.EXCLUDE;
import static io.effi.rpc.context.options.InterceptorOptions.INCLUDE;
import static io.effi.rpc.context.options.ResolverOptions.STAGE_CHAIN_RESOLVER;

/**
 * Provides default interceptor chain resolution for a peer.
 */
@SuppressWarnings("rawtypes")
@Extension(value = NAME, primary = true)
public class DefaultInterceptorChainResolver implements InterceptorChainResolver {

    public static final String NAME = Constant.DEFAULT_NAME;

    @Override
    public Interceptor.Chain resolveCallChain(PeerDescriptor descriptor, ScopedModule module) {
        return resolve(descriptor, module, ChainType.CALL);
    }

    @Override
    public Interceptor.Chain resolveChosenChain(PeerDescriptor descriptor, ScopedModule module) {
        if (descriptor.kind() != PeerDescriptor.Kind.CALLER) {
            return empty(module);
        }
        return resolve(descriptor, module, ChainType.CHOSEN);
    }

    @Override
    public Interceptor.Chain resolveReplyChain(PeerDescriptor descriptor, ScopedModule module) {
        return resolve(descriptor, module, ChainType.REPLY);
    }

    private Interceptor.Chain resolve(PeerDescriptor descriptor, ScopedModule module, ChainType type) {
        InteractionUnitClassifier<Interceptor> classifier = new InteractionUnitClassifier<>(descriptor);
        List<String> names = new LinkedList<>();
        Map<String, Interceptor> additionalInterceptors = new HashMap<>();
        registerClassifier(classifier, type, names);
        classifier.classify(lookupAvailableInterceptors(descriptor, module));
        addStageInterceptor(descriptor, module, type, names, additionalInterceptors);
        return ImmutableInterceptorChain.of(module, StringUtil.toArray(names), additionalInterceptors);
    }

    private void registerClassifier(InteractionUnitClassifier<Interceptor> classifier, ChainType type, List<String> names) {
        switch (type) {
            case CALL -> classifier.handler(CallInterceptorClassifier.handler((name, filter) -> names.add(name)));
            case CHOSEN -> classifier.handler(ChosenInterceptorClassifier.handler((name, filter) -> names.add(name)));
            case REPLY -> classifier.handler(ReplyInterceptorClassifier.handler((name, filter) -> names.add(name)));
        }
    }

    private Map<String, Interceptor> lookupAvailableInterceptors(PeerDescriptor descriptor, ScopedModule module) {
        Collection<String> configuredInterceptors = CollectionUtil.toHashSet(descriptor.options().option(INCLUDE));
        String[] excludedNames = descriptor.options().option(EXCLUDE);
        Set<String> excludedInterceptors = CollectionUtil.toHashSet(excludedNames);
        return module.namedExtensions(Interceptor.class, (name, holder) ->
                (configuredInterceptors.contains(name) || holder.hasTags(Tags.FORCE_ACTIVE))
                        && !excludedInterceptors.contains(name)
        );
    }

    private void addStageInterceptor(PeerDescriptor descriptor, ScopedModule module, ChainType type, List<String> names, Map<String, Interceptor> additionalInterceptors) {
        StageChainResolver resolver = module.preferredExtension(
                StageChainResolver.class,
                descriptor.options().option(STAGE_CHAIN_RESOLVER)
        );
        Stage.Chain head = type == ChainType.REPLY
                ? resolver.resolveReplyChain(descriptor, module)
                : resolver.resolveCallChain(descriptor, module);
        String stageName = switch (type) {
            case CALL -> CallInterceptorStage.NAME;
            case CHOSEN -> ChosenInterceptorStage.NAME;
            case REPLY -> ReplyInterceptorStage.NAME;
        };
        tryAddStageInterceptor(head, stageName, names, additionalInterceptors);
    }

    private void tryAddStageInterceptor(Stage.Chain head, String stageName, List<String> interceptorNames, Map<String, Interceptor> additionalInterceptors) {
        if (head instanceof ImmutableStageChain headChain) {
            ImmutableStageChain chain = headChain.lookupChain(stageName);
            if (chain != null && chain.next() != null) {
                StageInterceptor stageInterceptor = StageInterceptor.create(chain.next());
                additionalInterceptors.put(stageInterceptor.name(), stageInterceptor);
                interceptorNames.add(stageInterceptor.name());
            }
        }
    }

    private Interceptor.Chain empty(ScopedModule module) {
        return ImmutableInterceptorChain.of(module, StringUtil.emptyArray());
    }

    private enum ChainType {
        CALL,
        CHOSEN,
        REPLY
    }
}
