package io.effi.rpc.context;

import io.effi.rpc.component.ScopedModule;
import io.effi.rpc.component.support.ThreadPool;
import io.effi.rpc.config.ConfigurableOptionName;
import io.effi.rpc.config.HierarchicalOptions;
import io.effi.rpc.config.OptionName;
import io.effi.rpc.config.QueryPath;
import io.effi.rpc.util.Attributes;
import io.effi.rpc.trait.Identifiable;
import io.effi.rpc.util.TypeCapture;

import static io.effi.rpc.config.OptionName.Strategy.CURRENT_FIRST;
import static io.effi.rpc.config.OptionName.Strategy.MERGE_PARENT;

/**
 * Defines a peer that shares behaviors of caller and servant.
 * <p>
 * Provides a common interface for both calling and called sides of interactions,
 * including protocol, query path, thread pool, and execution chain management.
 *
 * @see Caller
 * @see Servant
 */
public interface Peer extends Attributes, Identifiable, Protocol.Supplier, HierarchicalOptions.Supplier, ScopedModule.Supplier {

    OptionName<String> PATH = ConfigurableOptionName.nameOf("path", MERGE_PARENT);

    OptionName<String> ASSOCIATED_MODULE = ConfigurableOptionName.nameOf("associatedModule", CURRENT_FIRST);

    OptionName<String> ANNOTATION_STYLE = ConfigurableOptionName.nameOf("annotationStyle", CURRENT_FIRST);

    OptionName<String> SERIALIZER = ConfigurableOptionName.nameOf("serializer", CURRENT_FIRST);

    OptionName<Long> SERIALIZATION_THRESHOLD = ConfigurableOptionName.nameOf("serializationThreshold", CURRENT_FIRST);

    OptionName<Long> DESERIALIZATION_THRESHOLD = ConfigurableOptionName.nameOf("deserializationThreshold", CURRENT_FIRST);

    OptionName<String> COMPRESSOR = ConfigurableOptionName.nameOf("compressor", CURRENT_FIRST);

    static String buildId(String protocol, String path) {
        return "(" + protocol + ")" + path;
    }

    /**
     * Returns the query path for this side.
     */
    QueryPath queryPath();

    /**
     * Returns the reply type.
     */
    TypeCapture<?> replyType();

    /**
     * Returns the thread pool for execution.
     */
    ThreadPool threadPool();

    /**
     * Returns the stage chain for execution.
     */
    Stage.Chain callStageChain();

    /**
     * Returns the reply stage chain.
     */
    Stage.Chain replyStageChain();

    /**
     * Returns the interceptor chain for calls.
     */
    Interceptor.Chain callInterceptorChain();

    /**
     * Returns the reply interceptor chain.
     */
    Interceptor.Chain replyInterceptorChain();
}




