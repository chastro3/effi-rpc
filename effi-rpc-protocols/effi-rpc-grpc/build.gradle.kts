plugins {
    id("java-library")
}

description = "gRPC protocol integration for Effi RPC."
dependencies {
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
    api(project(":effi-rpc-protocols:effi-rpc-http"))
}
