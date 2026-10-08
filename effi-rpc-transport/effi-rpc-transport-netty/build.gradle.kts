plugins {
    id("java-library")
}

description = "Netty-based transport implementation."
dependencies {
    api(project(":effi-rpc-transport:effi-rpc-transport-api"))
    api("io.netty:netty-codec-http2")
}
