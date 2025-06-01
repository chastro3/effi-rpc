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
    module("effi-rpc-annotation", { it.enablePublish() })
    module("effi-rpc-processor", { it.enablePublish() })
    module("effi-rpc-config", { it.enablePublish().enableProcessor() })
    module("effi-rpc-base", { it.enablePublish().enableProcessor() })
    module("effi-rpc-boot", { it.enablePublish().enableProcessor() })
    module("effi-rpc-governance", { it.enablePublish().enableProcessor() })
    module("effi-rpc-proxy", { it.enablePublish().enableProcessor() })
    module("effi-rpc-marshalling", { it.enablePublish().enableProcessor() })
    module("effi-rpc-metrics", { it.enablePublish().enableProcessor() })
    module("effi-rpc-protocols") {
        module("effi-rpc-http", { it.enablePublish().enableProcessor() })
        module("effi-rpc-grpc", { it.enablePublish().enableProcessor() })
    }
    module("effi-rpc-registry") {
        module("effi-rpc-registry-api", { it.enablePublish().enableProcessor() })
        module("effi-rpc-registry-consul", { it.enablePublish().enableProcessor() })
        module("effi-rpc-registry-nacos", { it.enablePublish().enableProcessor() })
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