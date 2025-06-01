package io.effi.rpc.constant;

/**
 * Holds constants for component-related names.
 */
public interface Component {

    /**
     * Default component name.
     */
    String DEFAULT = "default";

    /**
     * Proxy factory types.
     */
    interface ProxyFactory {

        /**
         * JDK dynamic proxy.
         */
        String JDK = "jdk";

        /**
         * CGLIB proxy.
         */
        String CGLIB = "cglib";

        /**
         * ByteBuddy proxy.
         */
        String BYTEBUDDY = "byteBuddy";
    }

    /**
     * Supported communication protocols.
     */
    interface Protocol {

        String HTTP_1_1 = "http/1.1";

        String HTTP_2 = "http/2.0";

        String GRPC = "grpc";
    }

    /**
     * Supported service registries.
     */
    interface Registry {

        /**
         * Zookeeper registry.
         */
        String ZOOKEEPER = "zookeeper";

        /**
         * Consul registry.
         */
        String CONSUL = "consul";

        /**
         * Nacos registry.
         */
        String NACOS = "nacos";

        /**
         * Redis-based registry.
         */
        String REDIS = "redis";
    }

    /**
     * Supported serialization methods.
     */
    interface Serialization {

        /**
         * JDK default serialization.
         */
        String JDK = "jdk";

        /**
         * JSON serialization.
         */
        String JSON = "json";

        /**
         * Fury serialization.
         */
        String FURY = "fury";

        /**
         * Kryo serialization.
         */
        String KRYO = "kryo";

        /**
         * MessagePack serialization.
         */
        String MSGPACK = "msgpack";

        /**
         * Protobuf serialization.
         */
        String PROTOBUF = "protobuf";
    }

    /**
     * Supported protocol mechanisms.
     */
    interface Transport {
        /**
         * Netty-based protocol.
         */
        String NETTY = "netty";
    }

    /**
     * Fault tolerance strategies.
     */
    interface FaultTolerance {

        /**
         * Retry on failure.
         */
        String FAIL_RETRY = "failRetry";

        /**
         * Fail fast without retry.
         */
        String FAIL_FAST = "failFast";

        /**
         * Retry on timeout.
         */
        String TIMEOUT_RETRY = "timeoutRetry";
    }

    /**
     * Load balancing strategies.
     */
    interface LoadBalance {

        /**
         * Random load balancing.
         */
        String RANDOM = "random";

        /**
         * Round-robin load balancing.
         */
        String ROUND_ROBIN = "roundRobin";

        /**
         * Weighted round-robin load balancing.
         */
        String WEIGHTED_ROUND_ROBIN = "WeightedRoundRobin";
    }

    /**
     * Supported compression algorithms.
     */
    interface Compression {

        /**
         * GZIP compression.
         */
        String GZIP = "gzip";

        /**
         * DEFLATE compression.
         */
        String DEFLATE = "deflate";

        /**
         * LZ4 compression.
         */
        String LZ4 = "lz4";

        /**
         * Snappy compression.
         */
        String SNAPPY = "snappy";
    }

    /**
     * Annotated programming styles.
     */
    interface AnnotationStyle {

        /**
         * JAX-RS (Java API for Restful Web Services) style.
         */
        String JAX_RS = "jax-rs";
    }

    /**
     * H2 clear text mode.
     */
    interface H2ClearTextMode {

        String H2C_MODE = "h2cMode";

        String PREFACE_MODE = "prefaceMode";

    }


}
