plugins {
    id("java-library")
}

description="HTTP protocol support."
dependencies {
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
    api(project(":effi-rpc-core"))
    api(project(":effi-rpc-transport:effi-rpc-transport-netty"))
}
