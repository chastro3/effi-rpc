plugins {
    id("java-library")
}

description = "Nacos registry implementation."
dependencies {
    api(project(":effi-rpc-registry:effi-rpc-registry-api"))
    api("com.alibaba.nacos:nacos-client")
}
