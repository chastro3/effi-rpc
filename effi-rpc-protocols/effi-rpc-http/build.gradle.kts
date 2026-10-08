plugins {
    id("java-library")
}

description = "HTTP/1 and HTTP/2 protocol support for Effi RPC."
dependencies {
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
    api(project(":effi-rpc-core"))
    api(project(":effi-rpc-transport:effi-rpc-transport-netty"))
}
