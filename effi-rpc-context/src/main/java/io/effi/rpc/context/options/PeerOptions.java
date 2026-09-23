package io.effi.rpc.context.options;

import io.effi.rpc.option.OptionName;

import static io.effi.rpc.option.OptionTypes.STRING;
import static io.effi.rpc.option.OptionTypes.STRING_ARRAY;

/**
 * Defines peer options shared by callers and servants.
 */
public interface PeerOptions {

    OptionName<String[]> PATH = STRING_ARRAY.mergeParent("peer.path");

    OptionName<String> ASSOCIATED_MODULE = STRING.currentFirst("peer.associatedModule");

    OptionName<String> ANNOTATION_STYLE = STRING.currentFirst("peer.annotationStyle");
}
