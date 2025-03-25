description = "Service registry implemented with nacos."
dependencies {
    api(project(":effi-rpc-registry:effi-rpc-registry-api"))
    api("com.alibaba.nacos:nacos-client")
}