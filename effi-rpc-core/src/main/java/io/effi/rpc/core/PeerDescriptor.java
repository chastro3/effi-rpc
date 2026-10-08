package io.effi.rpc.core;

import io.effi.rpc.config.QueryPath;
import io.effi.rpc.context.Protocol;
import io.effi.rpc.option.HierarchicalOptions;
import io.effi.rpc.util.AssertUtil;
import io.effi.rpc.util.TypeCapture;

/**
 * Describes an immutable peer definition.
 */
public record PeerDescriptor(
        /** Peer kind. */
        Kind kind,
        /** Owning protocol. */
        Protocol protocol,
        /** Query path. */
        QueryPath path,
        /** Reply type. */
        TypeCapture<?> replyType,
        /** Resolved peer options. */
        HierarchicalOptions options
) {

    public PeerDescriptor {
        AssertUtil.notNull(kind, "kind");
        AssertUtil.notNull(protocol, "protocol");
        AssertUtil.notNull(path, "path");
        AssertUtil.notNull(replyType, "replyType");
        AssertUtil.notNull(options, "options");
    }

    /**
     * Defines peer side kinds.
     */
    public enum Kind {
        CALLER,
        SERVANT
    }
}
