rootProject.name = "effi-rpc"

modules {
    module("effi-rpc-common", { it.extra["publish"] = true })
    module("effi-rpc-bom")
    module("effi-rpc-contract", { it.extra["publish"] = true })
    module("effi-rpc-governance", { it.extra["publish"] = true })
    module("effi-rpc-registry") {
        module("effi-rpc-registry-api", { it.extra["publish"] = true })
        module("effi-rpc-registry-consul", { it.extra["publish"] = true })
        module("effi-rpc-registry-nacos", { it.extra["publish"] = true })
    }
    module("effi-rpc-proxy") {
        module("effi-rpc-proxy-api", { it.extra["publish"] = true })
        module("effi-rpc-proxy-jdk", { it.extra["publish"] = true })
        module("effi-rpc-proxy-cglib", { it.extra["publish"] = true })
        module("effi-rpc-proxy-bytebuddy", { it.extra["publish"] = true })
    }
    module("effi-rpc-serialization") {
        module("effi-rpc-serialization-api", { it.extra["publish"] = true })
        module("effi-rpc-serialization-jdk", { it.extra["publish"] = true })
        module("effi-rpc-serialization-json", { it.extra["publish"] = true })
        module("effi-rpc-serialization-kryo", { it.extra["publish"] = true })
        module("effi-rpc-serialization-msgpack", { it.extra["publish"] = true })
        module("effi-rpc-serialization-protobuf", { it.extra["publish"] = true })
    }
    module("effi-rpc-metrics", { it.extra["publish"] = true })
    module("effi-rpc-engine") {
        module("effi-rpc-engine-api", { it.extra["publish"] = true })
        module("effi-rpc-engine-http", { it.extra["publish"] = true })
    }
    module("effi-rpc-test")
    module("effi-rpc-demo") {
        module("consumer")
        module("provider")
    }
    module("effi-rpc-transport") {
        module("effi-rpc-transport-api", { it.extra["publish"] = true })
        module("effi-rpc-transport-netty", { it.extra["publish"] = true })
    }
    module("effi-rpc-processor", { it.extra["publish"] = true })
}


class ModuleLoader(private val settings: Settings, private val prefix: String = "") {
    companion object {
        val moduleConfigs = mutableMapOf<String, (Project) -> Unit>()
    }

    fun module(name: String, configure: ((Project) -> Unit)? = null, children: ModuleLoader.() -> Unit = {}) {
        val path = if (prefix.isEmpty()) name else "$prefix:$name"
        settings.include(path)
        if (configure != null) moduleConfigs[":$path"] = configure
        ModuleLoader(settings, path).apply(children)
    }
}

fun Settings.modules(block: ModuleLoader.() -> Unit) {
    ModuleLoader(this).apply(block)
}


gradle.projectsLoaded {
    rootProject.subprojects.forEach {
        ModuleLoader.moduleConfigs[it.path]?.invoke(it)
    }
}
