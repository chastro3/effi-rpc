description = "Service registry implementation using Nacos."
dependencies {
    api(project(":effi-rpc-registry:effi-rpc-registry-api"))
    api("com.alibaba.nacos:nacos-client")
}