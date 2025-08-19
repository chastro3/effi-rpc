package io.effi.rpc.boot.confiurator;

import io.effi.rpc.annotation.component.Extension;
import io.effi.rpc.config.ConfigNames;
import io.effi.rpc.constant.Tags;
import io.effi.rpc.context.Peer;
import io.effi.rpc.context.ConfigurablePeer;
import io.effi.rpc.context.ConfigurableCaller;
import io.effi.rpc.context.ImmutableInterceptorChain;
import io.effi.rpc.context.ImmutableStageChain;
import io.effi.rpc.context.Interceptor;
import io.effi.rpc.context.Stage;
import io.effi.rpc.context.StageInterceptor;
import io.effi.rpc.boot.confiurator.stage.CallInterceptStage;
import io.effi.rpc.boot.confiurator.stage.ChosenInterceptStage;
import io.effi.rpc.boot.confiurator.stage.ReplyInterceptStage;
import io.effi.rpc.context.support.util.CallInterceptorClassifier;
import io.effi.rpc.context.support.util.ChosenInterceptorClassifier;
import io.effi.rpc.context.support.util.InteractionUnitClassifier;
import io.effi.rpc.context.support.util.ReplyInterceptorClassifier;
import io.effi.rpc.util.CollectionUtil;
import io.effi.rpc.util.StringUtil;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.effi.rpc.boot.confiurator.DefaultInterceptorChainConfigurator.NAME;

@SuppressWarnings("rawtypes")
@Extension(value = NAME, primary = true)
public class DefaultInterceptorChainConfigurator implements ConfigurablePeer.InterceptorChainConfigurator {

    public static final String NAME = ConfigNames.DEFAULT;

    @Override
    public void configure(ConfigurablePeer peer) {
        InteractionUnitClassifier<Interceptor> classifier = new InteractionUnitClassifier<>(peer);
        List<String> callNames = peer.callInterceptorChain() == null ? new LinkedList<>() : null;
        List<String> chosenNames = createChosenNamesIfNeed(peer);
        List<String> replyNames = peer.replyInterceptorChain() == null ? new LinkedList<>() : null;
        registerClassifiers(classifier, callNames, chosenNames, replyNames);
        classifier.classify(lookupAvailableInterceptors(peer));
        if (callNames != null) processCallInterceptors(peer, callNames);
        if (chosenNames != null) processChosenInterceptors(peer, chosenNames);
        if (replyNames != null) processReplyInterceptors(peer, replyNames);
    }

    private List<String> createChosenNamesIfNeed(Peer peer) {
        if (peer instanceof ConfigurableCaller<?> caller) {
            return caller.chosenInterceptorChain() == null ? new LinkedList<>() : null;
        }
        return null;
    }

    private void registerClassifiers(InteractionUnitClassifier<Interceptor> filterClassifier,
                                     List<String> callNames,
                                     List<String> chosenNames,
                                     List<String> replyNames) {
        if (callNames != null) {
            filterClassifier.handler(CallInterceptorClassifier.handler((name, filter) -> callNames.add(name)));
        }
        if (chosenNames != null) {
            filterClassifier.handler(ChosenInterceptorClassifier.handler((name, filter) -> chosenNames.add(name)));
        }
        if (replyNames != null) {
            filterClassifier.handler(ReplyInterceptorClassifier.handler((name, filter) -> replyNames.add(name)));
        }
    }

    private void processCallInterceptors(ConfigurablePeer peer, List<String> callNames) {
        tryAddStageInterceptor(peer.callStageChain(), CallInterceptStage.NAME, callNames);
        peer.withCallInterceptorChain(ImmutableInterceptorChain.of(peer.module(), StringUtil.toArray(callNames)));
    }

    private void processChosenInterceptors(ConfigurablePeer peer, List<String> chosenNames) {
        ConfigurableCaller<?> caller = (ConfigurableCaller<?>) peer;
        tryAddStageInterceptor(peer.callStageChain(), ChosenInterceptStage.NAME, chosenNames);
        caller.withChosenInterceptorChain(ImmutableInterceptorChain.of(peer.module(), StringUtil.toArray(chosenNames)));
    }

    private void processReplyInterceptors(ConfigurablePeer peer, List<String> replyNames) {
        tryAddStageInterceptor(peer.replyStageChain(), ReplyInterceptStage.NAME, replyNames);
        peer.withReplyInterceptorChain(ImmutableInterceptorChain.of(peer.module(), StringUtil.toArray(replyNames)));
    }


    private Map<String, Interceptor> lookupAvailableInterceptors(ConfigurablePeer peer) {
        List<String[]> configuredNames = peer.getMergedConfig(ConfigNames.INTERCEPTOR);
        Collection<String> configuredInterceptors = CollectionUtil.flatDistinctArray(configuredNames);
        String[] excludedNames = peer.getConfig(ConfigNames.EXCLUDED_INTERCEPTOR);
        Set<String> excludedInterceptors = CollectionUtil.toHashSet(excludedNames);
        return peer.module().namedExtensions(Interceptor.class, (name, holder) ->
                (configuredInterceptors.contains(name) || holder.hasTags(Tags.FORCE_ACTIVE))
                        && !excludedInterceptors.contains(name)
        );
    }

    private void tryAddStageInterceptor(Stage.Chain head, String stageName, List<String> interceptorNames) {
        if (head instanceof ImmutableStageChain headChain) {
            ImmutableStageChain chain = headChain.lookupChain(stageName);
            if (chain != null && chain.next() != null) {
                StageInterceptor stageInterceptor = StageInterceptor.lookup(chain.next());
                interceptorNames.add(stageInterceptor.name());
            }
        }
    }
}
