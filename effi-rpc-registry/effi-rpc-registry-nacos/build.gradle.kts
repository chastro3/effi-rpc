plugins {
    id("java-library")
}

description = "Nacos registry implementation for Effi RPC."
dependencies {
    api(project(":effi-rpc-registry:effi-rpc-registry-api"))
    api("com.alibaba.nacos:nacos-client")
}
