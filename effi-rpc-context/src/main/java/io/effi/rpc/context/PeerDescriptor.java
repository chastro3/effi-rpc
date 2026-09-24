package io.effi.rpc.context;

import io.effi.rpc.config.QueryPath;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.TypeCapture;

/**
 * Immutable description of a peer.
 */
public record PeerDescriptor(
        Kind kind,
        Protocol protocol,
        QueryPath path,
        TypeCapture<?> replyType,
        HierarchicalOptions options
) {

    public PeerDescriptor {
        AssertUtil.notNull(kind, "kind");
        AssertUtil.notNull(protocol, "protocol");
        AssertUtil.notNull(path, "path");
        AssertUtil.notNull(replyType, "replyType");
        AssertUtil.notNull(options, "options");
    }

    public enum Kind {
        CALLER,
        SERVANT
    }
}
