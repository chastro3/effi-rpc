pluginManagement {
    includeBuild("build-logic")
}
plugins {
    id("internal-module-loader")
}
rootProject.name = "effi-rpc"

modules {
    module("effi-rpc-bom", { it.enablePublish() })
    module("effi-rpc-common", { it.enablePublish() })
    module("effi-rpc-contract", { it.enablePublish() })
    module("effi-rpc-processor", { it.enablePublish() })
    module("effi-rpc-governance", { it.enablePublish().enableProcessor() })
    module("effi-rpc-registry") {
        module("effi-rpc-registry-api", { it.enablePublish().enableProcessor() })
        module("effi-rpc-registry-consul", { it.enablePublish().enableProcessor() })
        module("effi-rpc-registry-nacos", { it.enablePublish().enableProcessor() })
    }
    module("effi-rpc-proxy") {
        module("effi-rpc-proxy-api", { it.enablePublish().enableProcessor() })
        module("effi-rpc-proxy-jdk", { it.enablePublish().enableProcessor() })
        module("effi-rpc-proxy-cglib", { it.enablePublish().enableProcessor() })
        module("effi-rpc-proxy-bytebuddy", { it.enablePublish().enableProcessor() })
    }
    module("effi-rpc-serialization") {
        module("effi-rpc-serialization-api", { it.enablePublish().enableProcessor() })
        module("effi-rpc-serialization-jdk", { it.enablePublish().enableProcessor() })
        module("effi-rpc-serialization-json", { it.enablePublish().enableProcessor() })
        module("effi-rpc-serialization-kryo", { it.enablePublish().enableProcessor() })
        module("effi-rpc-serialization-msgpack", { it.enablePublish().enableProcessor() })
        module("effi-rpc-serialization-protobuf", { it.enablePublish().enableProcessor() })
    }
    module("effi-rpc-metrics", { it.enablePublish().enableProcessor() })
    module("effi-rpc-engine") {
        module("effi-rpc-engine-api", { it.enablePublish().enableProcessor() })
        module("effi-rpc-engine-http", { it.enablePublish().enableProcessor() })
    }
    module("effi-rpc-transport") {
        module("effi-rpc-transport-api", { it.enablePublish().enableProcessor() })
        module("effi-rpc-transport-netty", { it.enablePublish().enableProcessor() })
    }
    module("effi-rpc-test")
    module("effi-rpc-demo") {
        module("consumer", { it.enableProcessor() })
        module("provider", { it.enableProcessor() })
    }
}