description="HTTP protocol support."
dependencies {
    compileOnly("jakarta.ws.rs:jakarta.ws.rs-api")
    api(project(":effi-rpc-engine:effi-rpc-engine-api"))
    api(project(":effi-rpc-transport:effi-rpc-transport-netty"))
}