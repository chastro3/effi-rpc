plugins {
    id("java-library")
}

description = "Netty-based transport implementation for Effi RPC."
dependencies {
    api(project(":effi-rpc-transport:effi-rpc-transport-api"))
    api("io.netty:netty-codec-http2")
}
